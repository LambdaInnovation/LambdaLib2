/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib2.cgui.component;

import cn.lambdalib2.cgui.Widget;
import cn.lambdalib2.cgui.event.DragEvent;
import cn.lambdalib2.cgui.event.GuiEvent;

/**
 * Prefer using {@link DragBar} instead.
 * @author WeAthFolD
 */
@Deprecated
public class VerticalDragBar extends Component {
    
    public static class DraggedEvent implements GuiEvent {}
    
    public double y0, y1;

    public VerticalDragBar(double _y0, double _y1) {
        this();
        y0 = _y0;
        y1 = _y1;
    }

    public VerticalDragBar() {
        super("VerticalDragBar");
        
        listen(DragEvent.class, (w, event) -> {
            double originalX = w.transform.x;
            w.getGui().updateDragWidget();
            w.transform.x = originalX;
            w.getGui().updateWidget(w);
            
            if(w.transform.y > y1) {
                w.transform.y = y1;
            } else if(w.transform.y < y0) {
                w.transform.y = y0;
            }
            w.post(new DraggedEvent());
            w.dirty = true;
        });
    }
    
    public static VerticalDragBar get(Widget w) {
        return w.getComponent("VerticalDragBar");
    }
    
    public double getProgress() {
        return (widget.transform.y - y0) / (y1 - y0);
    }
    
    public void setProgress(double prg) {
        widget.transform.y = y0 + (y1 - y0) * prg;
        widget.dirty = true;
    }
    
    public VerticalDragBar setArea(double _y0,  double _y1) {
        y0 = _y0;
        y1 = _y1;
        
        return this;
    }
}
