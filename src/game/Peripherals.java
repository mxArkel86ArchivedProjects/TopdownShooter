package game;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.MouseInputListener;


public class Peripherals implements KeyListener, MouseInputListener, MouseWheelListener {
    public Map<Integer, Boolean> keyboard = new HashMap<Integer, Boolean>();
    Map<Integer, ScrollEvent> scroll_hook = new HashMap<Integer, ScrollEvent>();
    Map<Integer, MouseMoveEvent> mousemove_hook = new HashMap<Integer, MouseMoveEvent>();

    public boolean keyPressed(char c){
        int code = java.awt.event.KeyEvent.getExtendedKeyCodeForChar(c);
        if(keyboard.get(code)==null)
            return false;
        return keyboard.get(code);
    }

    public boolean keyPressed(int c){
        if(keyboard.get(c)==null)
            return false;
        return keyboard.get(c);
    }

    public int addScrollHook(ScrollEvent e){
        int i;
        for(i = 0;i<100;i++){
            if(scroll_hook.get(i)==null)
                break;
        }
        scroll_hook.put(i, e);
        return i;
    }

    public void removeScrollHook(int i){
        scroll_hook.remove(i);
    }
    public int addMouseMoveHook(MouseMoveEvent e){
        int i;
        for(i = 0;i<100;i++){
            if(mousemove_hook.get(i)==null)
                break;
        }
        mousemove_hook.put(i, e);
        return i;
    }

    public void removeMouseMoveHook(int i){
        mousemove_hook.remove(i);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int c =e.getKeyCode();
        keyboard.put(c, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int c =e.getKeyCode();
        keyboard.put(c, false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        e.consume();
        for(MouseMoveEvent ev : mousemove_hook.values()){
            ev.action(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        e.consume();
        
        double val = e.getPreciseWheelRotation();
        for(ScrollEvent ev : scroll_hook.values()){
            ev.action(val, e.getX(), e.getY());
        }
        
    }
    
}