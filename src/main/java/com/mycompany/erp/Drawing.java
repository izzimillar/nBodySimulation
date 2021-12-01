/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.erp;

/**
 *
 * @author izzimillar
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.lang.Math.*;
import java.util.ArrayList;


public class Drawing extends JFrame implements ActionListener {
    
    
    
    // close details thing programatically - click on planet when option pane is still there yk what i mean
    // zoom
    // move screen
    // show timer
    // speed up/slow down
    
    
    
    // all values are in kg and m
    // average distance from sun & mean orbital velocity used for the initial conditions
    
    // gets the size of the screen
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    private int time;
    static boolean relativeSizes = true;
    static Timer timer;
    
    DetailsPanel details;
    boolean showDetails = false;
    
    JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
    private int zoomFactor;
    
    // change these so they're relative to the screen and also can change them
    // scale factors to multiply the size and distances of the planets by to make it possible to see the distances and sizes relatively
    private double relativeDistanceSF = 3.2e9;
    private double relativeSizeSF = 2200;
    
    // instansiating the sun
    double solarMass = 1.9891e30;
    double[] sunVel = {0,0};
    double[] sunPos = {0,0};
    double sunRadius = 695508;
    Color sunColour = Color.yellow;
    Particle sun = new Particle(solarMass, sunVel, sunPos, sunRadius, sunColour);

    
    // creates a list of all the planets to perform the calculations on
    private Particle[] planets = startingValues();

    
    public static void main(String[] args) {
        new Drawing();
    }
    
    
    public Drawing() {
        // starts a timer that is used to update the position of the planets at every at point in time
        timer = new Timer(500/60, this);
        
        this.setSize(screenSize);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        PaintSurface canvas = new PaintSurface();
        this.add(canvas);
        this.add(new ButtonPanel(), BorderLayout.SOUTH);
               
        this.setVisible(true);
    }
        
    class PaintSurface extends JComponent {
                
        public PaintSurface() {
            this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    Point point = e.getPoint();
                    point.y = point.y - (int) screenSize.getHeight()/2;
                    for (Particle planet: planets) {
                        if (planet.getShape().contains(point)) {
                            // show details
                            details = new DetailsPanel(planet);
                            showDetails = true;
                        }
                    }

                    
                }
            });
        }
        
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, (int) screenSize.getWidth(), (int) screenSize.getHeight());
            
            // translates the origin to the middle of the left side
            g2.translate(0, screenSize.getHeight()/2);
            
            if (relativeSizes) {
                // loops through all the planets
                for (int i = 0; i < planets.length; i++) {
                    // calculates the relative diameter and radius of each planet using the scale factors
                    int diameter = (int) (planets[i].getSize()*2/relativeSizeSF);
                    int radius = (int) (planets[i].getSize()/relativeSizeSF);

                    // calculates a constant distance for the distances between the planet and the sun depending on its order
                    
                    int x = (int) ((planets[i].getPosition()[0]/(planets[i].getSF() * zoomFactor/5))*(i+1));
                    int y = (int) ((planets[i].getPosition()[1]/(planets[i].getSF() * zoomFactor/5))*(i+1));
                    
                    // draws a circle to represent the planet
                    Shape planetShape = new Ellipse2D.Float(x-radius, y-radius, diameter, diameter);
                    g2.setColor(planets[i].getColour());
                    g2.fill(planetShape);
                    planets[i].setShape(planetShape);
                    
                }
                // draws the sun at half its relative size (to fit it on the screen)

                int sunSize = (int) (sun.getSize()/relativeSizeSF);
                Shape sunShape = new Ellipse2D.Float(-sunSize/2, -sunSize/2, sunSize, sunSize);
                g2.setColor(sun.getColour());
                g2.fill(sunShape);
                
            } else {
                // draws an circle to represent each planet using the x and y positions and a scale factor to scale the system down
                // to a size that can be seen on the screen
                
                // sizes of the planet displayed on the screen
                int diameter = 5 * zoomFactor;
                int radius = 5 * zoomFactor / 2;
                
                for (Particle planet: planets) {
                    int x = (int) (planet.getPosition()[0]/relativeDistanceSF);
                    int y = (int) (planet.getPosition()[1]/relativeDistanceSF);

                    // draws a circle to represnet the planet with a size of 5 pixels in a relative position
                    Shape planetShape = new Ellipse2D.Float(x-radius, y-radius, diameter, diameter);
                    g2.setColor(planet.getColour());
                    g2.fill(planetShape);
                    planet.setShape(planetShape);
                }

                // draws the sun in the centre of the screen with a size of 10 pixels
                Shape sunShape = new Ellipse2D.Float(-diameter, -diameter, diameter * 2, diameter * 2);
                g2.setColor(sun.getColour());
                g2.fill(sunShape);
                
            }
        }
    }
    
    // function to update the forces on each of the planets at every time step
    private void update() {
        // sets the time period to 10000 seconds to use in calculating the force and velocity
        time = 10000;
        
        // loops through the list of planets to calculate the force exerted on them
        for (Particle planet: planets) {
            // sets the force to zero at the beginning of each time it is updated
            planet.resetForce();
            
            // calculates the force exerted on the planet by the sun
            planet.calculateForce(sun);
            
            // loops through the planets again to calculate force exerted on each planet by each other planet
            for (Particle planet2: planets) {
                if (planet != planet2) {
                    planet.calculateForce(planet2);
                }
            }
            
            // after the total force has been calculated updates the velocity and position of each particle
            planet.updateParticle(time);
        }

    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }
    
    class ButtonPanel extends JPanel {
        public ButtonPanel() {
            JButton sizeOrDistance = new JButton("switch");
            JButton start = new JButton("start");
            JButton stop = new JButton("stop");
            JButton restart = new JButton("restart");
            
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    switchRelative();
                }
            };
            
            ActionListener startFunc = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    timer.start();
                } 
            };
            
            ActionListener stopFunc = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    timer.stop();
                } 
            };
            
            ActionListener restartFunc = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    restartDraw();
                }
            };
            
                        
            slider.addChangeListener(e -> sliderChange());
            sizeOrDistance.addActionListener(listener);
            start.addActionListener(startFunc);
            stop.addActionListener(stopFunc);
            restart.addActionListener(restartFunc);
            
            this.add(slider);
            this.add(sizeOrDistance);
            this.add(start);
            this.add(stop);
            this.add(restart);
            this.setBackground(Color.BLACK);
        }
    }
    
    class DetailsPanel extends JDialog {
        public DetailsPanel(Particle planet) {
            JOptionPane pane = new JOptionPane("details");
            pane.setBounds(0,0,100,100);
            JDialog dialog = pane.createDialog((JFrame) null, planet.getName());
            dialog.setLocation(screenSize.width, 0);
            dialog.setVisible(true);
        }
    }
    
    public void switchRelative() {
        // method to switch the relativeSizes variable from true to false to change how the planets are being displayed
        relativeSizes = !relativeSizes;
        repaint();
    }
    
    public void restartDraw() {
        // method to set the planets back to their starting positions and repaint the screen
        planets = startingValues();
        repaint();
    }
    
    public void sliderChange() {
        int value = slider.getValue();
        zoomFactor = value;
        relativeSizeSF = 1000 * zoomFactor;
        relativeDistanceSF = 3.2e9 / zoomFactor;
        repaint();
    }
        
    public Particle[] startingValues() {


        // instansiating the earth
         double earthMass = 5.97219e24;
         double[] earthVel = {0,29782.78};
         double[] earthPos = {1.49598262e11, 0};
         double earthRadius = 6371;
         Color earthColour = Color.green;
         Particle earth = new Particle(earthMass, earthVel, earthPos, earthRadius, earthColour);
         earth.setName("Earth");


        // instansiating mercury
         double mercuryMass = 3.30104e23;
         double[] mercuryVel = {0,47361.94};
         double[] mercuryPos = {5.7909227e10, 0};
         double mercuryRadius = 2439.7;
         Color mercuryColour = Color.magenta;
         Particle mercury = new Particle(mercuryMass, mercuryVel, mercuryPos, mercuryRadius, mercuryColour);
         mercury.setName("Mercury");
         

        // instansiating venus
         double venusMass = 4.86732e24;
         double[] venusVel = {0, 35020.56};
         double[] venusPos = {1.08209475e11, 0};
         double venusRadius = 6051.8;
         Color venusColour = Color.orange;
         Particle venus = new Particle(venusMass, venusVel, venusPos, venusRadius, venusColour);
         venus.setName("Venus");


        // instansiating mars
         double marsMass = 6.41693e23;
         double[] marsVel = {0, 24076.94};
         double[] marsPos = {2.27943824e11, 0};
         double marsRadius = 3389.5;
         Color marsColour = Color.red;
         Particle mars = new Particle(marsMass, marsVel, marsPos, marsRadius, marsColour);
         mars.setName("Mars");


        // instansiating jupiter
         double jupiterMass = 1.89813e27;
         double[] jupiterVel = {0, 13056.11};
         double[] jupiterPos = {7.78340821e11, 0};
         double jupiterRadius = 69911;
         Color jupiterColour = Color.orange;
         Particle jupiter = new Particle(jupiterMass, jupiterVel, jupiterPos, jupiterRadius, jupiterColour);
         jupiter.setName("Jupiter");


        // instansiating saturn
         double saturnMass = 5.68319e26;
         double[] saturnVel = {0, 9639.17};
         double[] saturnPos = {1.426666422e12, 0};
         double saturnRadius = 58232;
         Color saturnColour = Color.yellow;
         Particle saturn = new Particle(saturnMass, saturnVel, saturnPos, saturnRadius, saturnColour);
         saturn.setName("Saturn");


        // instansiating uranus
         double uranusMass = 8.68103e25;
         double[] uranusVel = {0, 6799.17};
         double[] uranusPos = {2.870658186e12, 0};
         double uranusRadius = 25362;
         Color uranusColour = Color.cyan;
         Particle uranus = new Particle(uranusMass, uranusVel, uranusPos, uranusRadius, uranusColour);
         uranus.setName("Uranus");


        // instansiating neptune
         double neptuneMass = 1.0241e26;
         double[] neptuneVel = {0, 5435};
         double[] neptunePos = {4.498396441e12, 0};
         double neptuneRadius = 24622;
         Color neptuneColour = Color.blue;
         Particle neptune = new Particle(neptuneMass, neptuneVel, neptunePos, neptuneRadius, neptuneColour);
         neptune.setName("Neptune");

        Particle[] planets = {mercury, venus, earth, mars, jupiter, saturn, uranus, neptune};
        return planets;

    }
}
