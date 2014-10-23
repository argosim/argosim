#!/usr/bin/python

# this script is used to convert a saved IGV (genome browser) session
# into one that has nicely formatted tracks (i.e. right order, colors
# and height).

# author: Bernhard Knasmueller

from xml.dom import minidom
import sys
filename = sys.argv[1]
Test_file = open(filename,'r')
dom = minidom.parse(Test_file)
Test_file.close()

panels = dom.getElementsByTagName("Panel")
count = 1
tracks = panels[0].getElementsByTagName("Track")
for track in tracks:
    # change colors of tracks
    if count == 1:
        track.setAttribute('color', "255,51,0")
    elif count == 2:
        track.setAttribute('color', "51,204,0")
    else:
        track.setAttribute('color', "153,153,0")
    # change heights of tracks
    track.setAttribute('height', "78")

    # change track names
    identifier = track.attributes["id"].value
    if "ISS.wig.bw" in identifier:
        track.setAttribute("name", "ISS")
    if "AMB.wig.bw" in identifier:
        track.setAttribute("name", "AMB")
    if "MSD.wig.bw" in identifier:
        track.setAttribute("name", "MSD")
    count += 1

# adjust order (ISS - AMB - MSD)
(dom.getElementsByTagName("Panel"))[0].appendChild( tracks[0] )
(dom.getElementsByTagName("Panel"))[0].appendChild( tracks[2] )


open(filename,"wb").write(dom.toxml())
