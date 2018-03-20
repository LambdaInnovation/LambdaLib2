/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib2.cgui.event;

/**
 * Solely a notification event, fired on current focus when it was dragged.
 * 
 * @author WeAthFolD
 */
public class DragEvent implements GuiEvent {
    
    /**
     * Offset coordinates from dragging widget origin to current mouse position, in global scale level.
     */
    public final double offsetX, offsetY;

    public DragEvent(double _offsetX, double _offsetY) {
        offsetX = _offsetX;
        offsetY = _offsetY;
    }

}
