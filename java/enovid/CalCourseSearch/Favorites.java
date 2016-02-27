package enovid.CalCourseSearch;

import java.util.ArrayList;


public class Favorites  {
    private ArrayList<Courses> list = new ArrayList<>();

    public Favorites()  {

    }

    public ArrayList<Courses> getList() {
        return list;
    }

    public void setFavorites(ArrayList<Courses> favorites) {
        this.list = favorites;
    }

    public void addFavorite(Courses c)   {
        boolean duplicate = false;
        for (int i = 0; i < list.size(); i++){
            String cid = c.getDept() + c.getNum();
            Courses current = list.get(i);
            String current_id = current.getDept() + current.getNum();
            if (cid.equals(current_id))
            {
                duplicate = true;
            }
        }
        if (!duplicate)
        {
            list.add(c);
        }
    }

    public int getLength() {
        return list.size();
    }


    public Courses find(String id) {
        for (int i = 0; i < this.list.size(); i++)
        {
            Courses c = list.get(i);
            String name = c.getDept() + c.getNum();
            if (id.equals(name))
                return c;
        }
        return new Courses();
    }

    public void remove(int index)
    {
        this.list.remove(index);
    }
}
