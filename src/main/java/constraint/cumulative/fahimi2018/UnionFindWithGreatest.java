/*
@author Arthur Godet <arth.godet@gmail.com>
@since 23/05/2019
*/
package constraint.cumulative.fahimi2018;

import gnu.trove.list.array.TIntArrayList;

public class UnionFindWithGreatest extends UnionFind<Integer> {

    private TIntArrayList greatests;

    public UnionFindWithGreatest(int n) {
        greatests = new TIntArrayList(n);
        reset(n);
    }

    @Override
    public void union(Integer element1, Integer element2) {
        int greatest = Math.max(findGreatest(element1), findGreatest(element2));
        super.union(element1, element2);
        greatests.set(find(element1), greatest);
    }

    public int findGreatest(int element) {
        return greatests.getQuick(find(element));
    }

    public void reset(int n) {
        for(int i = 0; i<n; i++) {
            this.parentMap.put(i, i);
            this.rankMap.put(i, 0);
        }
        this.count = n;
        greatests.clear();
        for(int i = 0; i<n; i++) {
            greatests.add(i);
        }
    }
}
