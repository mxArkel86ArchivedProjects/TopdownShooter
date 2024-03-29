package game.managers;

import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.MouseInputListener;

import util.KeyPressEvent;
import util.MouseEvent;
import util.ScrollEvent;
import util.TypeEvent;

public class Peripherals implements KeyListener, MouseInputListener, MouseWheelListener {
    public static Peripherals PERI = new Peripherals();

    public Map<Integer, Boolean> keyboard = new HashMap<Integer, Boolean>();
    Map<Integer, ScrollEvent> scroll_hook = new HashMap<Integer, ScrollEvent>();
    Map<Integer, TypeEvent> keytype_hook = new HashMap<Integer, TypeEvent>();
    Map<Integer, KeyPressEvent> keypress_hook = new HashMap<Integer, KeyPressEvent>();
    Map<Integer, MouseEvent> mousemove_hook = new HashMap<Integer, MouseEvent>();
    Map<Integer, MouseEvent> mouseclick_hook = new HashMap<Integer, MouseEvent>();

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
    public int addTypeHook(TypeEvent e){
        int i;
        for(i = 0;i<100;i++){
            if(keytype_hook.get(i)==null)
                break;
        }
        keytype_hook.put(i, e);
        return i;
    }

    public void removeTypeHook(int i){
        keytype_hook.remove(i);
    }

    public int addKeyPressHook(KeyPressEvent e){
        int i;
        for(i = 0;i<100;i++){
            if(keypress_hook.get(i)==null)
                break;
        }
        keypress_hook.put(i, e);
        return i;
    }

    public void removeKeyPressHook(int i){
        keypress_hook.remove(i);
    }

    public int addMouseMoveHook(MouseEvent e){
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
    public int addMouseClickHook(MouseEvent e){
        int i;
        for(i = 0;i<100;i++){
            if(mouseclick_hook.get(i)==null)
                break;
        }
        mouseclick_hook.put(i, e);
        return i;
    }

    public void removeMouseClickHook(int i){
        mouseclick_hook.remove(i);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c =e.getKeyChar();
        e.consume();
        for(TypeEvent ev : keytype_hook.values()){
            ev.action(c, e.getKeyCode());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int c =e.getKeyCode();
        char c_ = e.getKeyChar();
        keyboard.put(c, true);
        for(KeyPressEvent ev : keypress_hook.values()){
            ev.action(c_, c, true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int c =e.getKeyCode();
        char c_ = e.getKeyChar();
        keyboard.put(c, false);
        for(KeyPressEvent ev : keypress_hook.values()){
            ev.action(c_, c, true);
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

    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        
    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        e.consume();
        for(MouseEvent ev : mouseclick_hook.values()){
            ev.action(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {
        
    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
        
    }

    @Override
    public void mouseDragged(java.awt.event.MouseEvent e) {
        
    }

    @Override
    public void mouseMoved(java.awt.event.MouseEvent e) {
        e.consume();
        for(MouseEvent ev : mousemove_hook.values()){
            ev.action(e.getX(), e.getY());
        }
    }
    
}