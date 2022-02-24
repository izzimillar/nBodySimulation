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
import java.net.*;



public class Drawing extends JFrame implements ActionListener {
    
    
    // close details thing programatically - click on planet when option pane is still there yk what i mean
    // move screen (ik exactly how to do it but my key listener isn't working right now which is jaring)
    
    
    // all values are in kg and m
    // I have used the average distance from sun & mean orbital velocity from the NASA website for the initial conditions
    
    // gets the size of the screen
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    // keeps track of the relative time in days that the simulation has been running
    private double daysSinceStart = 0;

    // integer for the time changed each time the screen updates
    // this controls how much the planets move each time the timer goes off
    private double timeStep = 10000;

    // boolean to keep track of whether the relative sizes or relative distances are being shown
    static boolean relativeSizes = true;

    // the timer that controls the simulation
    static Timer timer;

    // the label that shows how long the simulation has been running relatively
    JLabel timeLabel;
    
    // the details panel that is displayed when a planet is clicked on
    DetailsPanel details;

    // boolean to keep track of whether the details panel is currently being shown or not
    boolean showDetails = false;
    
    // the slider that controls how zoomed in the simulation is
    // from 1-10, starting at 3
    JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 10, 3);

    // the number that indicates the zoom factor of the simulation from the slider
    private int zoomFactor = 3;
    
    // scale factors to multiply the size and distances of the planets by to make it possible to see the distances and sizes relatively
    private double relativeDistanceSF = 4e9/3;
    private double relativeSizeSF = 5000/3;

    // the inital x and y positions of the origin ie where the centre of the sun will be drawn
    private int xPosition = 0;
    private int yPosition = screenSize.height/2;
    
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
        // the timer goes off approximately 100 times a second
        timer = new Timer(500/60, this);
        
        // sets the size of the simulation to the screen size so it covers the whole screen
        this.setSize(screenSize);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        PaintSurface canvas = new PaintSurface();
        this.add(canvas);
        this.add(new ButtonPanel(), BorderLayout.SOUTH);
               
        this.setVisible(true);

        this.addKeyListener(new KeyLis());

    }
        
    class PaintSurface extends JComponent {
                
        public PaintSurface() {
            this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    showDetails = false;

                    // gets the point that the mouse has been clicked
                    Point point = e.getPoint();

                    // minuses the current y position from the y value to account for the translation of the origin
                    point.y = point.y - yPosition;
                    // changes the x component of the point to account for any translation of the origin
                    point.x = point.x - xPosition;
                    for (Particle planet: planets) {
                        // if the point that was clicked is inside a planet
                        if (planet.getShape().contains(point)) {
                            // show details
                            showDetails = true;
                            details = new DetailsPanel(planet);
                            repaint();
                        }
                    }
                }
            });
        }
        
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int temp;
            
            // draws a black rectangle the size of the screen so the background is black
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, (int) screenSize.getWidth(), (int) screenSize.getHeight());
            
            // translates the origin to the middle of the left side
            g2.translate(xPosition, yPosition);
            
            if (relativeSizes) {
                // draws the sun at its relative size

                int sunSize = (int) ((sun.getSize() * 2)/relativeSizeSF);
                Shape sunShape = new Ellipse2D.Float(-sunSize/2, -sunSize/2, sunSize, sunSize);
                g2.setColor(sun.getColour());
                g2.fill(sunShape);
                
                // loops through all the planets
                temp = sunSize/2 + 36*zoomFactor;
                for (int i = 0; i < planets.length; i++) {
                    // calculates the relative diameter and radius of each planet using the scale factors
                    int diameter = (int) (planets[i].getSize()*2/relativeSizeSF);
                    int radius = (int) (planets[i].getSize()/relativeSizeSF);

                    // calculates a constant distance for the distances between the planet and the sun depending on its order
                    int x = (int) (planets[i].getPosition()[0]/(planets[i].getSF(temp)));
                    int y = (int) (planets[i].getPosition()[1]/(planets[i].getSF(temp)));
                    
                    // draws a circle to represent the planet
                    Shape planetShape = new Ellipse2D.Float(x-radius, y-radius, diameter, diameter);
                    g2.setColor(planets[i].getColour());
                    g2.fill(planetShape);
                    planets[i].setShape(planetShape);

                    temp += 50*zoomFactor;
                    temp += diameter/relativeSizeSF;
                    
                }
                
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
            planet.updateParticle(timeStep);
        }

        daysSinceStart += timeStep/(60*60*24);

        timeLabel.setText("days since start: " + (int) daysSinceStart);
    }
    
    @Override
    // when the timer goes off
    public void actionPerformed(ActionEvent e) {
        // updates the particles data
        update();
        // repaints the screen
        repaint();
    }
    
    // does not work i need to fix this but idk how because its not even saying its been pressed atm LOL
    private class KeyLis extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    System.out.println("left");
                    // xPosition += 100;
                    // repaint();
                    break;
                case KeyEvent.VK_RIGHT:
                    xPosition -= 100;
                    repaint();
                    break;
            }
        }

    }

    class ButtonPanel extends JPanel {
        public ButtonPanel() {
            JButton sizeOrDistance = new JButton("switch");
            JButton start = new JButton("start");
            JButton stop = new JButton("stop");
            JButton restart = new JButton("restart");
            timeLabel = new JLabel("days since start: " + (int) daysSinceStart);
            JButton speed = new JButton("faster");
            JButton slow = new JButton("slower");
            JButton left = new JButton("left");
            left.setActionCommand("left");
            JButton right = new JButton("right");
            right.setActionCommand("right");

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

            ActionListener speedUp = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    timeStep = Math.min(timeStep * 2, 320000.0);
                }
            };
            
            ActionListener slowDown = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    timeStep = Math.max(timeStep / 2, 2500);
                }
            };

            ActionListener move = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    moveCentre(event.getActionCommand());
                }
            };
                        
            slider.addChangeListener(e -> sliderChange());
            sizeOrDistance.addActionListener(listener);
            start.addActionListener(startFunc);
            stop.addActionListener(stopFunc);
            restart.addActionListener(restartFunc);
            speed.addActionListener(speedUp);
            slow.addActionListener(slowDown);
            left.addActionListener(move);
            right.addActionListener(move);

            timeLabel.setForeground(Color.white);
            
            this.add(slider);
            this.add(sizeOrDistance);
            this.add(start);
            this.add(stop);
            this.add(restart);
            this.add(timeLabel);
            this.add(speed);
            this.add(slow);
            this.add(left);
            this.add(right);
            this.setBackground(Color.BLACK);
        }
    }
    
    class DetailsPanel extends JPanel {
        // updates the details panel with the relevant information when a planet is clicked on
        public DetailsPanel(Particle planet) {
            // adds a link to the panel
            String msg = "<html><u>more details</u></html>";
            JLabel link = new JLabel(msg);
            // sets the cursor to be a hand when moving over the link
            link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            // gets the link to the relevant planets page on solarsytem.nasa.gov
            String url = planet.getLink();
            // creates a uri from this link
            URI uri = URI.create(url);
            link.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // if the user has clicked on the link
                    try {
                        // try to open the link in the browser
                        Desktop.getDesktop().browse(uri);
                    } catch (Exception e1) {
                        // prints if theres an error
                        e1.printStackTrace();
                    }
                }

            });

            // adds the link to the panel
            JPanel message = new JPanel();
            message.add(link);

            // creates an option pane with the content on
            JOptionPane pane = new JOptionPane(message);
            
            JDialog dialog = pane.createDialog((JFrame) null, planet.getName());
            dialog.setLocation(screenSize.width, 0);
            // dialog.setSize(300, 150);
            dialog.setVisible(showDetails);            
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
        daysSinceStart = 0;
        xPosition = 0;
        repaint();
    }
    
    public void sliderChange() {
        int value = slider.getValue();
        // sets the zoom factor to the value currently on the slider
        zoomFactor = value;
        // updates the two scale factors and repaints the 
        relativeSizeSF = 5000 / zoomFactor;
        relativeDistanceSF = 4e9 / zoomFactor;
        repaint();
    }
    
    public void moveCentre(String direction) {
        // moves the centre of the drawing 100 pixels a direction depending on which button was pressed
        // xPosition -= 100;
        if (direction == "left") {
            xPosition -= 100;
        } else if (direction == "right") {
            xPosition += 100;
        }

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
        earth.setLink("https://solarsystem.nasa.gov/planets/earth/overview/");


    // instansiating mercury
        double mercuryMass = 3.30104e23;
        double[] mercuryVel = {0,47361.94};
        double[] mercuryPos = {5.7909227e10, 0};
        double mercuryRadius = 2439.7;
        Color mercuryColour = Color.magenta;
        Particle mercury = new Particle(mercuryMass, mercuryVel, mercuryPos, mercuryRadius, mercuryColour);
        mercury.setName("Mercury");
        mercury.setLink("https://solarsystem.nasa.gov/planets/mercury/overview/");
        

    // instansiating venus
        double venusMass = 4.86732e24;
        double[] venusVel = {0, 35020.56};
        double[] venusPos = {1.08209475e11, 0};
        double venusRadius = 6051.8;
        Color venusColour = Color.orange;
        Particle venus = new Particle(venusMass, venusVel, venusPos, venusRadius, venusColour);
        venus.setName("Venus");
        venus.setLink("https://solarsystem.nasa.gov/planets/venus/overview/");

    // instansiating mars
        double marsMass = 6.41693e23;
        double[] marsVel = {0, 24076.94};
        double[] marsPos = {2.27943824e11, 0};
        double marsRadius = 3389.5;
        Color marsColour = Color.red;
        Particle mars = new Particle(marsMass, marsVel, marsPos, marsRadius, marsColour);
        mars.setName("Mars");
        mars.setLink("https://solarsystem.nasa.gov/planets/mars/overview/");


    // instansiating jupiter
        double jupiterMass = 1.89813e27;
        double[] jupiterVel = {0, 13056.11};
        double[] jupiterPos = {7.78340821e11, 0};
        double jupiterRadius = 69911;
        Color jupiterColour = Color.orange;
        Particle jupiter = new Particle(jupiterMass, jupiterVel, jupiterPos, jupiterRadius, jupiterColour);
        jupiter.setName("Jupiter");
        jupiter.setLink("https://solarsystem.nasa.gov/planets/jupiter/overview/");


    // instansiating saturn
        double saturnMass = 5.68319e26;
        double[] saturnVel = {0, 9639.17};
        double[] saturnPos = {1.426666422e12, 0};
        double saturnRadius = 58232;
        Color saturnColour = Color.yellow;
        Particle saturn = new Particle(saturnMass, saturnVel, saturnPos, saturnRadius, saturnColour);
        saturn.setName("Saturn");
        saturn.setLink("https://solarsystem.nasa.gov/planets/saturn/overview/");


    // instansiating uranus
        double uranusMass = 8.68103e25;
        double[] uranusVel = {0, 6799.17};
        double[] uranusPos = {2.870658186e12, 0};
        double uranusRadius = 25362;
        Color uranusColour = Color.cyan;
        Particle uranus = new Particle(uranusMass, uranusVel, uranusPos, uranusRadius, uranusColour);
        uranus.setName("Uranus");
        uranus.setLink("https://solarsystem.nasa.gov/planets/uranus/overview/");


    // instansiating neptune
        double neptuneMass = 1.0241e26;
        double[] neptuneVel = {0, 5435};
        double[] neptunePos = {4.498396441e12, 0};
        double neptuneRadius = 24622;
        Color neptuneColour = Color.blue;
        Particle neptune = new Particle(neptuneMass, neptuneVel, neptunePos, neptuneRadius, neptuneColour);
        neptune.setName("Neptune");
        neptune.setLink("https://solarsystem.nasa.gov/planets/neptune/overview/");

        Particle[] planets = {mercury, venus, earth, mars, jupiter, saturn, uranus, neptune};
        return planets;

    }
}
