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
import cn.lambdalib2.util.MathUtils;

public class DragBar extends Component {

    public static class DraggedEvent implements GuiEvent {}

    public enum Axis { X, Y }

    /**
     * Lower and upper bound of the drag area.
     */
    public double lower, upper;
    public Axis axis = Axis.Y;

    public DragBar(double _y0, double _y1) {
        this();
        lower = _y0;
        upper = _y1;
    }

    public DragBar() {
        super("DragBar");

        listen(DragEvent.class, (w, event) -> {
            double original;
            if (axis == Axis.X) {
                original = w.transform.y;
            } else {
                original = w.transform.x;
            }

            w.getGui().updateDragWidget();

            if (axis == Axis.X) {
                w.transform.y = original;
                w.transform.x = MathUtils.clampd(lower, upper, w.transform.x);
            } else {
                w.transform.x = original;
                w.transform.y = MathUtils.clampd(lower, upper, w.transform.y);
            }

            w.getGui().updateWidget(w);
            w.post(new DraggedEvent());
        });
    }

    public static DragBar get(Widget w) {
        return w.getComponent("DragBar");
    }

    public double getProgress() {
        double ret;
        if (axis == Axis.X) {
            ret = (widget.transform.x - lower) / (upper - lower);
        } else {
            ret = (widget.transform.y - lower) / (upper - lower);
        }

        return MathUtils.clampd(0, 1, ret);
    }

    public void setProgress(double prg) {
        double val = lower + (upper - lower) * prg;
        if (axis == Axis.X) {
            widget.transform.x = val;
        } else {
            widget.transform.y = val;
        }

        widget.dirty = true;
    }

    public DragBar setArea(double _lower,  double _upper) {
        lower = _lower;
        upper = _upper;

        return this;
    }

}
