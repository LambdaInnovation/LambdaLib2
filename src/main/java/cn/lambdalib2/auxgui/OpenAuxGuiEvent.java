package cn.lambdalib2.auxgui;


import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired whenever an new AuxGui is opened.
 * @author WeAthFolD
 */
public class OpenAuxGuiEvent extends Event {
    
    public final AuxGui gui;

    public OpenAuxGuiEvent(AuxGui _gui) {
        gui = _gui;
    }

}
