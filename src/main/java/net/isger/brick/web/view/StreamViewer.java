package net.isger.brick.web.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.isger.brick.ui.Screen;

public class StreamViewer extends AbstractViewer {

    public void render(Screen content, HttpServletRequest request, HttpServletResponse response) {

    }

    /**
     * 输入流
     *
     * @return
     */
    public InputStream getInputStream(Screen screen) {
        try {
            Object pending = screen.see("@stream");
            if (pending instanceof byte[]) {
                return new ByteArrayInputStream((byte[]) pending);
            } else if (pending instanceof String) {
                return new FileInputStream((String) pending);
            } else if (pending instanceof File) {
                return new FileInputStream((File) pending);
            } else if (pending instanceof InputStream) {
                return (InputStream) pending;
            }
        } catch (FileNotFoundException e) {
        }
        return null;
    }

}
