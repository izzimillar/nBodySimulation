/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.erp;

import java.lang.Math;
import java.awt.*;
/**
 *
 * @author izzi
 */
public class Particle {
    private double G = 6.67408e-11;
    private double mass;
    private double radius;
    private double[] vel;
    private double[] pos;
    private double[] force = {0,0};
    private Color colour;
    private Shape shape;
    
    private String name;
    private String link;
    
    // constructor class
    public Particle(double m, double[] v, double[] p, double r, Color c) {
        mass = m;
        vel = v;
        pos = p;
        radius = r;
        colour = c;
    }
    
    // returns the mass of the planet
    public double getMass() {
        return mass;
    }
    
    // returns the position of the planet (in the form [x pos, y pos]
    public double[] getPosition() {
        return pos;
    }
    
    // calculates the scale factor that scales the distance down to a constant size
    public double getSF(int idealDistance) {
        double distancex = this.pos[0];
        double distancey = this.pos[1];
        
        // pythagorus
        double distance = Math.sqrt((distancex*distancex) + (distancey*distancey));
        
        // returns a scalefactor that will be used to scale the actual distance to the ideal distance
        return distance/idealDistance;
    }
    
    public double getSize() {
        return radius;
    }
    
    public Color getColour() {
        return colour;
    }
    
    public Shape getShape() {
        return shape;
    }
    
    public String getName() {
        return name;
    }
    
    public String getLink() {
        return link;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setLink(String link) {
        this.link = link;
    }
    
    public void setPosition(double x, double y) {
        this.pos[0] = x;
        this.pos[1] = y;
    }
        
    public void calculateForce(Particle body2) {
        // calculates the distance between the 2 bodies
        double distancex = this.pos[0] - (body2.getPosition()[0]);
        double distancey = this.pos[1] - (body2.getPosition()[1]);        
        
        double distance = Math.sqrt((distancex*distancex) + (distancey*distancey));
        
        
        // calculates the angle between the 2 bodies so the force can be resolved into its x and y components
        double anglex = Math.acos(distancex/distance);
        double angley = Math.asin(distancey/distance);        
        
        
        // calculates the force between the 2 bodies using the inverse square law
        double massProduct = this.mass * body2.getMass();
        double wholeForce = massProduct/(Math.max(distance*distance, 1.496e11/10));
        wholeForce = G * wholeForce * -1;
        
        // calculates the x and y components of the force
        force[0] += (wholeForce * Math.cos(anglex));
        force[1] += (wholeForce * Math.sin(angley));
    }
    
    public void updateParticle(double timeStep) {
        // calculates the velocity of the body using v = Ft/m
        vel[0] += force[0] * timeStep / mass;
        vel[1] += force[1] * timeStep / mass;
                        
        // calculates the position of the body, using d = vt
        pos[0] += timeStep * vel[0];
        pos[1] += timeStep * vel[1];
        
        // NB: these numbers are averages, assuming the acceleration is constant between each time step
                

    }
    
    public void resetForce() {
        force[0] = 0;
        force[1] = 0;
    }
}
