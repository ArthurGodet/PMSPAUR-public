/*
@author Arthur Godet <arth.godet@gmail.com>
@since 21/02/2019
*/
package data.output;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import data.input.Instance;
import data.input.Job;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SolutionPMSPAUR {

    private Instance instance;
    private int[] processingTimesMachines;
    private LinkedList<Job>[] machines;

    @JsonCreator
    public SolutionPMSPAUR(
        @JsonProperty("instance") Instance instance,
        @JsonProperty("processingTimesMachines") int[] processingTimesMachines,
        @JsonProperty("machines") LinkedList<Job>[] machines) {
        this.instance = instance;
        this.processingTimesMachines = processingTimesMachines;
        this.machines = machines;
    }

    public SolutionPMSPAUR(Instance instance) {
        this.instance = instance;
        this.processingTimesMachines = new int[instance.getNbMachines()];
        this.machines = new LinkedList[instance.getNbMachines()];
        Arrays.fill(machines, new LinkedList<>());
    }

    private static void writeExcelFile(String path, XSSFWorkbook wb) {
        try {
            File file = new File(path);

            // --- Write current workbook in the main.java.data.output
            FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath());
            wb.write(fileOut);
            fileOut.close();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                wb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Instance getInstance() {
        return instance;
    }

    public int[] getProcessingTimesMachines() {
        return processingTimesMachines;
    }

    public LinkedList<Job>[] getMachines() {
        return machines;
    }

    public void add(int m, Job j) {
        machines[m].add(j);
        processingTimesMachines[m] += j.getProcTime();
    }

    @JsonIgnore
    public int getMakespan() {
        int c = 0;
        for (int procTimeMachine : processingTimesMachines) {
            c = Math.max(c, procTimeMachine);
        }
        return c;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < processingTimesMachines.length; i++) {
            sb.append(machines[i]).append(" : ").append(processingTimesMachines[i]).append("\n");
        }
        return sb.toString();
    }

    private CellStyle cellStyle(Workbook wb, int id) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        if (id == 1) { // if color is white
            style.setFillForegroundColor((short) instance.getListResources().size());
        } else {
            style.setFillForegroundColor((short) id);
        }
        if (id == 0) { // if color is black
            Font font = wb.createFont();
            font.setColor((short) 1);
            style.setFont(font);
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    public void toExcelFile(String path) {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("ordoResults");
        for (int j = 0; j < getMakespan() + 1; j++) {
            sheet.setColumnWidth(j, 256 * 4); // 256 = 11 pixels | 44 pixels allow to write 3 digits numbers
        }
        for (int i = 0; i < machines.length; i++) {
            Row row = sheet.createRow(i);
            int idx = 0;
            for (Job job : machines[i]) {
                for (int j = idx; j < idx + job.getProcTime(); j++) {
                    Cell c = row.createCell(j, CellType.NUMERIC);
                    c.setCellValue(job.getProcTime());
                    c.setCellStyle(cellStyle(wb, job.getResourceID()));
                }
                if (job.getProcTime() > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(i, i, idx, idx + job.getProcTime() - 1));
                }
                idx += job.getProcTime();
            }
        }
        writeExcelFile(path, wb);
    }
}
