# Overview

The GUI can be broken into three tasks:
 * *Display* notions and relations as visual things
   (such as ellipses with text) on screen
 * Choose a *layout* for those visual things
 * Allow the user to *control* what they see

Each of these tasks is briefly discussed below.

# Display

The display part is probably the most intuitively
clear one. Just draw an ellipse with text to
represent a Notion, and a line with arrow to
represent a Relation. If you want to use some
other shapes later, you can change them without
influencing any other part of the program. The
only important choice I see here is the trade-off
between:
 * Each Notion is displayed at most once. If you
   are looking at a shape-with-text on the screen
   you know that you are seeing everyting there
   is to know about it
 * The verb of a Relation is displayed
   independently of the verbs of all other
   Relations. A verb such as "is a" could be
   a part of so many relations that having no
   more than one at a time could potentially
   create a tangled mess on the screen

The display functionality is currently
implemented by the GraphView class, with each
Notion only shown at most once.

# Layout

Layout can either be decided automatically or
chosen by the user through dragging things around
 -- with multiple alternatives for each of these
options or anything in between. What's the best
choice does not seem remotely obvious.

My current thinking is to have a currently-active
Notion displayed in the center and the related
Notions, up to a certain depth, layed out
automatically around it. In addition, a Notion
could be pinned, so that it remains displayed
even if the active Notion changes to something
only distantly related. Pinned Notions can be
dragged around by the user, while the others
cannot.

# Control

See the discussion of Layout -- choices made
there would influence what kind of control we
are looking for.
