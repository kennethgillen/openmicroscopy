#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
FOR TRAINING PURPOSES ONLY!
"""

import omero
from omero.model.enums import UnitsLength
from omero.rtypes import rdouble, rint, rstring
from omero.gateway import BlitzGateway
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import imageId
imageId = int(imageId)


# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
updateService = conn.getUpdateService()


# Create ROI.
# =================================================================
# We are using the core Python API and omero.model objects here, since ROIs
# are not yet supported in the Python Blitz Gateway.
#
# First we load our image and pick some parameters for shapes
x = 50
y = 200
width = 100
height = 50
image = conn.getObject("Image", imageId)
theZ = image.getSizeZ() / 2
theT = 0


# We have a helper function for creating an ROI and linking it to new shapes
def createROI(img, shapes):
    # create an ROI, link it to Image
    roi = omero.model.RoiI()
    # use the omero.model.ImageI that underlies the 'image' wrapper
    roi.setImage(img._obj)
    for shape in shapes:
        roi.addShape(shape)
    # Save the ROI (saves any linked shapes too)
    updateService.saveObject(roi)


# Another helper for generating the color integers for shapes
def rgbaToRGBInt(red, green, blue, alpha=255):
    """ Convert an R,G,B,A value to an int """
    RGBAInt = (alpha << 24) + (red << 16) + (green << 8) + blue
    return int(RGBAInt)


# create a rectangle shape (added to ROI below)
print ("Adding a rectangle at theZ: %s, theT: %s, X: %s, Y: %s, width: %s,"
       " height: %s" % (theZ, theT, x, y, width, height))
rect = omero.model.RectangleI()
rect.x = rdouble(x)
rect.y = rdouble(y)
rect.width = rdouble(width)
rect.height = rdouble(height)
rect.theZ = rint(theZ)
rect.theT = rint(theT)
rect.textValue = rstring("test-Rectangle")

# create an Ellipse shape (added to ROI below)
ellipse = omero.model.EllipseI()
ellipse.cx = rdouble(y)
ellipse.cy = rdouble(x)
ellipse.rx = rdouble(width)
ellipse.ry = rdouble(height)
ellipse.theZ = rint(theZ)
ellipse.theT = rint(theT)
ellipse.textValue = rstring("test-Ellipse")

# Create an ROI containing 2 shapes on same plane
# NB: OMERO.insight client doesn't support this
# The ellipse is removed later (see below)
createROI(image, [rect, ellipse])

# create an ROI with single line shape
line = omero.model.LineI()
line.x1 = rdouble(x)
line.x2 = rdouble(x+width)
line.y1 = rdouble(y)
line.y2 = rdouble(y+height)
line.theZ = rint(theZ)
line.theT = rint(theT)
line.textValue = rstring("test-Line")
createROI(image, [line])

# create an ROI with single point shape
point = omero.model.PointI()
point.cx = rdouble(x)
point.cy = rdouble(y)
point.theZ = rint(theZ)
point.theT = rint(theT)
point.textValue = rstring("test-Point")
createROI(image, [point])


def pointsToString(points):
    """ Returns strange format supported by Insight """
    points = ["%s,%s" % (p[0], p[1]) for p in points]
    csv = ", ".join(points)
    return "points[%s] points1[%s] points2[%s]" % (csv, csv, csv)
# create an ROI with a single polygon, setting colors and lineWidth
polygon = omero.model.PolygonI()
polygon.theZ = rint(theZ)
polygon.theT = rint(theT)
polygon.fillColor = rint(rgbaToRGBInt(255, 255, 255, 125))
polygon.strokeColor = rint(rgbaToRGBInt(0, 255, 0))
polygon.strokeWidth = omero.model.LengthI(10, UnitsLength.PIXEL)
points = [[10, 20], [50, 150], [200, 200], [250, 75]]
polygon.points = rstring(pointsToString(points))
createROI(image, [polygon])

# Retrieve ROIs linked to an Image.
# =================================================================
roiService = conn.getRoiService()
result = roiService.findByImage(imageId, None)
for roi in result.rois:
    print "ROI:  ID:", roi.getId().getValue()
    for s in roi.copyShapes():
        shape = {}
        shape['id'] = s.getId().getValue()
        shape['theT'] = s.getTheT().getValue()
        shape['theZ'] = s.getTheZ().getValue()
        if s.getTextValue():
            shape['textValue'] = s.getTextValue().getValue()
        if type(s) == omero.model.RectangleI:
            shape['type'] = 'Rectangle'
            shape['x'] = s.getX().getValue()
            shape['y'] = s.getY().getValue()
            shape['width'] = s.getWidth().getValue()
            shape['height'] = s.getHeight().getValue()
        elif type(s) == omero.model.EllipseI:
            shape['type'] = 'Ellipse'
            shape['cx'] = s.getCx().getValue()
            shape['cy'] = s.getCy().getValue()
            shape['rx'] = s.getRx().getValue()
            shape['ry'] = s.getRy().getValue()
        elif type(s) == omero.model.PointI:
            shape['type'] = 'Point'
            shape['cx'] = s.getCx().getValue()
            shape['cy'] = s.getCy().getValue()
        elif type(s) == omero.model.LineI:
            shape['type'] = 'Line'
            shape['x1'] = s.getX1().getValue()
            shape['x2'] = s.getX2().getValue()
            shape['y1'] = s.getY1().getValue()
            shape['y2'] = s.getY2().getValue()
        elif type(s) in (
                omero.model.MaskI, omero.model.LabelI, omero.model.PolygonI):
            print type(s), " Not supported by this code"
        # Do some processing here, or just print:
        print "   Shape:",
        for key, value in shape.items():
            print "  ", key, value,
        print ""


# Remove shape from ROI
# =================================================================
result = roiService.findByImage(imageId, None)
for roi in result.rois:
    for s in roi.copyShapes():
        # Find and remove the Shape we added above
        if s.getTextValue() and s.getTextValue().getValue() == "test-Ellipse":
            print "Removing Shape from ROI..."
            roi.removeShape(s)
            roi = updateService.saveAndReturnObject(roi)


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
