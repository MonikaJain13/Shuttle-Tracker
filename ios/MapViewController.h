//
//  MapViewController.h
//  Shuttle-Tracker
//
//  Created by Brendon Justin on 1/29/11.
//  Copyright 2011 Brendon Justin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "DataManager.h"


@class KMLParser, JSONParser;

@interface MapViewController : UIViewController <MKMapViewDelegate> {
	MKMapView *_mapView;
    
    DataManager *dataManager;
    
    KMLParser *routeKmlParser;
    KMLParser *vehiclesKmlParser;
    
    NSURL *shuttleJSONUrl;
    
    NSArray *routes;
    NSArray *stops;
    
    NSMutableArray *vehicles;
    
    NSMutableArray *routeLines;
    NSMutableArray *routeLineViews;
    
    NSTimer *etaUpdateTimer;
	
	UIImage *shuttleImage;
    
}

@property (nonatomic, assign) DataManager *dataManager;
@property (nonatomic, assign) NSMutableArray *vehicles;


@end
