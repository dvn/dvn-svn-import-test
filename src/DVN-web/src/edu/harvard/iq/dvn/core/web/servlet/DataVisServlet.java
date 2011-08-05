/*
 * Dataverse Network - A web application to distribute, share and analyze quantitative data.
 * Copyright (C) 2011
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation,Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

/*
 * DataVisServlet.java
 *
 * Created on July 28, 2011, 2:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.web.servlet;


import java.awt.FontFormatException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import java.io.OutputStream;
import java.io.PrintWriter;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URL;
import java.net.URLDecoder;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.IIOException;

import java.util.logging.Logger;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author landreev
 * uses Steve Kraffmiller's code for generating visualization graph images
 *
 */
public class DataVisServlet extends HttpServlet {

    /** Creates a new instance of DataVisServlet */
    public DataVisServlet() {
    }

    /** Sets the logger (use the package name) */
    private static Logger dbgLog = Logger.getLogger(DataVisServlet.class.getPackage().getName());


    private String graphTitle = "";
    private String yAxisLabel = "";
    private String sources = "";
    private String heightCode = "";
    private Integer heightInt = new Integer(0);


    public void service(HttpServletRequest req, HttpServletResponse res) {

        // Parameters:

        String googleImageURL = req.getParameter("googleImageUrl");
        graphTitle = req.getParameter("graphTitle");
        yAxisLabel = req.getParameter("yAxisLabel");
        sources = req.getParameter("sources");
        heightCode = req.getParameter("heightCode");
        heightInt = new Integer(heightCode);
        OutputStream out = null;

        try {

            String decoded = URLDecoder.decode(googleImageURL, "UTF-8");



            out = res.getOutputStream();

            if (!decoded.isEmpty()){
                URL imageURLnew = new URL(googleImageURL);
                System.out.println("googleImageURL " + googleImageURL);
                try{
                    BufferedImage image =     ImageIO.read(imageURLnew);
                    BufferedImage combinedImage = getCompositeImage(image);

                    // MIME type header:

                    res.setHeader("Content-Type", "image/png");
                    // image content:

                    ImageIO.write(combinedImage, "png", out);

                } catch (IIOException io){
                    // TODO:
                    // log diagnostic messages;
                    // provide more info in the error response;
                     System.out.println(io.getMessage().toString());
                     System.out.println(io.getCause().toString());
                     System.out.println("IIOException " + imageURLnew);
                    createErrorResponse404(res);
                } catch (FontFormatException ff){
                    System.out.println("FontFormatException " + imageURLnew);
                    
                    System.out.println("FontFormatException " + ff.toString());
                }
            }


        } catch (UnsupportedEncodingException uee){
            // TODO:
            // log diagnostic messages;
            // provide more info in the error response;
            createErrorResponse404(res);

        } catch (MalformedURLException mue){
            // TODO:
            // log diagnostic messages;
            // provide more info in the error response;
        } catch (IOException io){
            // TODO:
            // log diagnostic messages;
            // provide more info in the error response;
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException x) {}
        }

        return;
    } // end of the main service() method.


    // The 3 methods below, compositeImage, rotateImage and stringToImage, have
    // been cut-and-pasted from ExploreDataPage by Steve Kraffmiller;
    // In the long run, we'll want this code to be in one place and not
    // duplicated. For now, I'm just trying to get it all to work.

    private BufferedImage getCompositeImage(BufferedImage image) throws FontFormatException, IOException{ Integer heightAdjustment = new Integer(0);
        if (this.heightInt == 1){
            heightAdjustment = 40;
        }
        if (this.heightInt == 3){
            heightAdjustment = -100;
        }        
        
        BufferedImage yAxisImage = new BufferedImage(100, 500, BufferedImage.TYPE_INT_ARGB);
        BufferedImage combinedImage = new BufferedImage(776, 575 + heightAdjustment , BufferedImage.TYPE_INT_ARGB);

       
        File retFile = generateImageString("16", "676x", "South", "0", graphTitle);       
        BufferedImage titleImage =     ImageIO.read(retFile);

        String source = "";

        if (!sources.trim().isEmpty()) {
             source = "Source: " + sources;
        }
        
        retFile = generateImageString("14", "676x", "NorthWest", "0", source);        
        BufferedImage sourceImage =     ImageIO.read(retFile);
        
        retFile = generateImageString("14", "200x", "South", "-90", yAxisLabel);        
        BufferedImage yAxisVertImage =     ImageIO.read(retFile);
        
        Graphics2D yag2 = yAxisImage.createGraphics();
        Graphics2D cig2 = combinedImage.createGraphics();

        Graphics2D sig2 = sourceImage.createGraphics();
        

        cig2.setColor(Color.WHITE);
        yag2.setColor(Color.WHITE);
        yag2.fillRect(0, 0, 676, 500);
        cig2.fillRect(0, 0, 776, 550);
       


        cig2.drawImage(yAxisImage, 0, 0, null);
        cig2.drawImage(yAxisVertImage, 0, 120 + heightAdjustment/2 , null);
        cig2.drawImage(image, 50, 50, null);
        cig2.drawImage(titleImage, 50, 0, null);
        cig2.drawImage(sourceImage, 50, 475 + heightAdjustment, null);

        yag2.dispose();
        sig2.dispose();
        cig2.dispose();        
        
        return combinedImage;
    }
   
    private void writeStringToImage(BufferedImage imageIn, 
            Graphics2D gr, String stringIn, boolean center, int startHeight, int startWidth){

        int width = imageIn.getWidth();
        int height = imageIn.getHeight();
        String splitString1 = "";
        String splitString2 = "";
        FontRenderContext context = gr.getFontRenderContext();
        Double strWidth = new Double (gr.getFont().getStringBounds(stringIn, context).getWidth());        
                
        if (strWidth > width){            
            int strLen = stringIn.length();
            int half = stringIn.length()/2;
            int nextSpace = stringIn.indexOf(" ",  half);                
            splitString1 = stringIn.substring(0, nextSpace);
            splitString2 = stringIn.substring(nextSpace + 1, strLen);
        }
        if (strWidth < width){
            if (center){
                Double halfWidth = new Double(Math.round(strWidth/2));
                int iHalf = halfWidth.intValue();
                int startpoint = width/2 - (iHalf);
                gr.drawString(stringIn, startpoint, startHeight * 2);
            } else {
                gr.drawString(stringIn, startWidth, startHeight);
            }
        }  else {
            if (center){
                Double strWidth1 = new Double (gr.getFont().getStringBounds(splitString1, context).getWidth()); 
                Double strWidth2 = new Double (gr.getFont().getStringBounds(splitString2, context).getWidth()); 
                Double halfWidth1 = new Double(Math.round(strWidth1/2));
                Double halfWidth2 = new Double(Math.round(strWidth2/2));
                int iHalf1 = halfWidth1.intValue();
                int iHalf2 = halfWidth2.intValue();
                int startpoint1 = width/2 - (iHalf1);
                int startpoint2 = width/2 - (iHalf2);
                gr.drawString(splitString1, startpoint1, startHeight);
                gr.drawString(splitString2, startpoint2, startHeight * 2);
            } else {
                gr.drawString(splitString1, startWidth, startHeight);
                gr.drawString(splitString2, startWidth, startHeight * 2);
            }
            
        }
        
        
    }


    private BufferedImage rotateImage(BufferedImage bufferedImage) {

        AffineTransform transform = new AffineTransform();
        transform.rotate((3.*Math.PI)/2., bufferedImage.getWidth()/2, bufferedImage.getHeight()/2);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        bufferedImage = op.filter(bufferedImage, null);
        return bufferedImage;
    }


    // This method can be used to send extra diagnostics information with
    // error responses:

    private void createErrorResponseGeneric(HttpServletResponse res, int status, String statusLine) {
        res.setContentType("text/html");

        if (status == 0) {
            status = 200;
        }

        res.setStatus(status);
        PrintWriter out = null;
        try {
            out = res.getWriter();
            out.println("<HTML>");
            out.println("<HEAD><TITLE>File Download</TITLE></HEAD>");
            out.println("<BODY>");
            out.println("<BIG>" + statusLine + "</BIG>");

            if (status == res.SC_NOT_FOUND) {
                for (int i = 0; i < 10; i++) {
                    out.println("<!-- This line is filler to handle IE case for 404 errors   -->");
                }
            }

            out.println("</BODY></HTML>");
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
    
    private File generateImageString() throws IOException {
        // let's attempt to generate the Text image:
        int exitValue = 0;
        File file = File.createTempFile("imageString","tmp");
        System.out.println(new File("/usr/bin/convert").exists() );
        if (new File("/usr/bin/convert").exists()) {           
            
            String ImageMagick = "/bin/sh -c \"/usr/bin/convert  -background white  -font Helvetica " +
                    "-pointsize 14  -gravity center  -size 676x  caption:'Graph Title'" +
                    " png:" + file.getAbsolutePath() + "\"";
            
            try {
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec(ImageMagick);
                exitValue = process.waitFor();
            } catch (Exception e) {
                exitValue = 1;
            }

            if (exitValue == 0) {
                return file;
            }

            return file;
        }

        return null;
    }

    
    private File generateImageString(String size, String width, String orientation, String rotate, String inStr) throws IOException {
        // let's attempt to generate the Text image:
        int exitValue = 0;
        File file = File.createTempFile("imageString","tmp");
        System.out.println(new File("/usr/bin/convert").exists() );
        if (new File("/usr/bin/convert").exists()) {           
            
            String ImageMagick = "/usr/bin/convert  -background white  -font Helvetica " +
                    "-pointsize 14  -gravity center  -size 676x  caption:\'Graph Title\'" +
                    " png:" + file.getAbsolutePath();
            
            String ImageMagickCmd[] = new String[15];
            
            ImageMagickCmd[0] = "/usr/bin/convert";
            ImageMagickCmd[1] = "-background";
            ImageMagickCmd[2] = "white";
            ImageMagickCmd[3] = "-font";
            ImageMagickCmd[4] = "Helvetica";
            ImageMagickCmd[5] = "-pointsize";
            ImageMagickCmd[6] = size;
            ImageMagickCmd[7] = "-gravity";
            ImageMagickCmd[8] = orientation;
            ImageMagickCmd[9] = "-rotate";
            ImageMagickCmd[10] = rotate;
            ImageMagickCmd[11] = "-size";
            ImageMagickCmd[12] = width;
            ImageMagickCmd[13] = "caption:" + inStr;
            ImageMagickCmd[14] = "png:" + file.getAbsolutePath();

                       
            
            try {
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec(ImageMagickCmd);
                exitValue = process.waitFor();
            } catch (Exception e) {
                exitValue = 1;
            }

            if (exitValue == 0) {
                return file;
            }

            return file;
        }

        return null;
    }

    private void createErrorResponse404(HttpServletResponse res) {
        createErrorResponseGeneric(res, res.SC_NOT_FOUND, "Sorry. The file you are looking for could not be found.");
        //createRedirectResponse(res, "/dvn/faces/ErrorPage.xhtml?errorMsg=Sorry. The file you are looking for could not be found.&errorCode=404");
    }

}

