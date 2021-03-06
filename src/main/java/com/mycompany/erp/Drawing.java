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
import java.lang.Math;


public class Drawing extends Canvas implements ActionListener {
    //  ALL NUMBERS NEED TO BE IN KG AND M!!!!!!!!
    // av distance from sun & mean orbital velocity used
    
    private int time;
    static boolean showDistance = true;
    
    // constants to x the size of the screen and masses of bodies by to make it as sort of accurate as possible
    private double relativeDistanceSF = 3.2e9;
    private double relativeSizeSF = 2200;
    
    
    // instansiating the sun
    private double solarMass = 1.9891e30;
    private double[] sunVel = {0,0};
    private double[] sunPos = {0,0};
    private double sunRadius = 695508;
    private Particle sun = new Particle(solarMass, sunVel, sunPos, sunRadius);

    
    // instansiating the earth
    private double earthMass = 5.97219e24;
    private double[] earthVel = {0,29782.78};
    private double[] earthPos = {1.49598262e11, 0};
    private double earthRadius = 6371;
    private Particle earth = new Particle(earthMass, earthVel, earthPos, earthRadius);
    
    
    // instansiating mercury
    private double mercuryMass = 3.30104e23;
    private double[] mercuryVel = {0,47361.94};
    private double[] mercuryPos = {5.7909227e10, 0};
    private double mercuryRadius = 2439.7;
    private Particle mercury = new Particle(mercuryMass, mercuryVel, mercuryPos, mercuryRadius);
    
    
    // instansiating venus
    private double venusMass = 4.86732e24;
    private double[] venusVel = {0, 35020.56};
    private double[] venusPos = {1.08209475e11, 0};
    private double venusRadius = 6051.8;
    private Particle venus = new Particle(venusMass, venusVel, venusPos, venusRadius);
    
    
    // instansiating mars
    private double marsMass = 6.41693e23;
    private double[] marsVel = {0, 24076.94};
    private double[] marsPos = {2.27943824e11, 0};
    private double marsRadius = 3389.5;
    private Particle mars = new Particle(marsMass, marsVel, marsPos, marsRadius);
    
    
    // instansiating jupiter
    private double jupiterMass = 1.89813e27;
    private double[] jupiterVel = {0, 13056.11};
    private double[] jupiterPos = {7.78340821e11, 0};
    private double jupiterRadius = 69911;
    private Particle jupiter = new Particle(jupiterMass, jupiterVel, jupiterPos, jupiterRadius);
    
    
    // instansiating saturn
    private double saturnMass = 5.68319e26;
    private double[] saturnVel = {0, 9639.17};
    private double[] saturnPos = {1.426666422e12, 0};
    private double saturnRadius = 58232;
    private Particle saturn = new Particle(saturnMass, saturnVel, saturnPos, saturnRadius);
    
    
    // instansiating uranus
    private double uranusMass = 8.68103e25;
    private double[] uranusVel = {0, 6799.17};
    private double[] uranusPos = {2.870658186e12, 0};
    private double uranusRadius = 25362;
    private Particle uranus = new Particle(uranusMass, uranusVel, uranusPos, uranusRadius);

    
    // instansiating neptune
    private double neptuneMass = 1.0241e26;
    private double[] neptuneVel = {0, 5435};
    private double[] neptunePos = {4.498396441e12, 0};
    private double neptuneRadius = 24622;
    private Particle neptune = new Particle(neptuneMass, neptuneVel, neptunePos, neptuneRadius);
    
    
    // creates a list of all the planets to perform the calculations on
    private Particle[] planets = {mercury, venus, earth, mars, jupiter, saturn, uranus, neptune};
    
    private Particle[] bigPlanets = {jupiter, saturn, uranus, neptune};
    private Particle[] smallPlanets = {mercury, venus, earth, mars};
    
    // earth's moon huh
//    private double moonMass = 7.34767309245735e22;
//    private double[] moonVel = {0,(29782.78+1022.36)};
//    private double[] moonPos = {(1.49598262e11-3.844e8),0};
//    private Particle moon = new Particle(moonMass, moonVel, moonPos);

    public Drawing() {
        // starts a timer that is used updates every at some point in time
        Timer timer = new Timer(500/60, this);
        timer.start();
    }
    
    public void paint(Graphics g) {
        
        if (showDistance) {
            g.translate(0, 370);
            
            for (int i = 0; i < planets.length; i++) {
                int diameter = (int) (planets[i].getSize()*2/relativeSizeSF);
                int radius = (int) (planets[i].getSize()/relativeSizeSF);
                
                int x = (int) ((planets[i].getPosition()[0]/planets[i].getSF())*(i+1));
                int y = (int) ((planets[i].getPosition()[1]/planets[i].getSF())*(i+1));
                
                g.fillOval(x-radius, y-radius, diameter, diameter);
            }
            
            int sunSize = (int) (sun.getSize()/relativeSizeSF);
            g.fillOval(-sunSize/2, -sunSize/2, sunSize, sunSize);
        } else {
            g.translate(0,370);

            // draws an circle to represent each planet using the x and y positions and a scale factor to scale the system down
            // to a size that can be seen on the screen
            for (Particle planet: planets) {
                int x = (int) (planet.getPosition()[0]/relativeDistanceSF);
                int y = (int) (planet.getPosition()[1]/relativeDistanceSF);
                g.fillOval(x, y, 5, 5);
            }
            
            // draws the sun in the centre of the screen with a size double that of the planets
            g.fillOval(-5, -5, 10, 10);
        }
    }
    
    private void update() {
        // sets the time period to 10000 to use in calculating the force and velocity
        time = 10000;
        
        // loop through the list of planets to calculate the force exerted on them
        for (Particle planet: planets) {
            // sets the force to zero at the beginning of each time it is updated
            planet.resetForce();
            
            // calculates the force exerted by the sun
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
//        System.out.println("");
        update();
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("n-body simulation");
        
        JButton button = new JButton("switch");
        
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String str = event.getActionCommand();
                System.out.println("clicked " + str);
                showDistance = !showDistance;
            }
        };
        
        button.setActionCommand("action command 1");
        button.addActionListener(listener);
        button.setBounds(1300,700,80,30);
        frame.add(button);
        
        Canvas canvas = new Drawing();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        canvas.setSize(1500, 800);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
        
        
    }
}
