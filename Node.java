import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class Node extends Circle {
    private String name;
    private boolean selected;

    public Node(double x, double y, double radius, Paint fill, String name){
        super(x, y, radius, fill);
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public boolean isSelected(){
        return selected;
    }

    public void setSelected(Boolean change){
        selected = change;
    }

    public void flip(){
        if(selected){
            setFill(Color.BLUE);
            setSelected(false);
        }
        else{
            setFill(Color.RED);
            setSelected(true);
        }
    }

    public String toString(){
        return getName();
    }

}

