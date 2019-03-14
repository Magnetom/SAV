package odyssey.projects.sav.db;

import java.util.ArrayList;
import java.util.List;

public class Track {

    public Track() { points_list = new ArrayList<>(); }

    Long    id;
    String  name;
    List<LocationPoint> points_list;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LocationPoint> getPoints_list() {
        return points_list;
    }
}
