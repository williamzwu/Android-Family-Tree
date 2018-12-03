
/*

To do:

create a bounding box anchor: done 7/27/2015

refactor to have model-conrol-view: 7/29/2015

make vertex position update to redraw: done if vertex order does not change. 7/29/2015 Disallowing angle order change. 7/31/2015

Output positions of vertex so that a static version can be published. done 8/2/2015

Refactor to make star.html general enough to be used for any family. The author needs only modify stonestory_unicode.js for his own family. 8/2/2015.

Make a static (no move) version of the document

Defects:
  For DINK family, sometimes the arc flips to outside. That's because the center is on the line between the two points. The choosing algorithm using line equation value sign does not work because for the center the calculated line value is 0. It seems that the points outside (on the left when going clock-wise) always have the negative line equation value. This seems to fix the problem but not proven mathematically yet. 7/30/2015
  
  When two persons are very far, the edge arc may cross the mid-line, need to detect it and make arc radius bigger. Fixed. 8/1/2015 A minimum gap between the arc and the mid-point between two vertices can be specified. With that, the minimum radious of the connecting arc can be calculated. See in-line comments in Edge class for exact mathematical proof.

*/

function Point( x, y )
{
	if ( x instanceof Point ) {
		this.X = x.X;
		this.Y = x.Y;
	} else {
		this.X = x;
		this.Y = y;
	}
	this.Add = function (ap) { this.X += ap.X; this.Y += ap.Y; return this; };
	this.Sub = function (ap) { this.X -= ap.X; this.Y -= ap.Y; return this; };
	this.Shift = function( x, y ) { this.X += x; this.Y += y; return this; };
  this.func = [];
  this.RegisterEventListener = function (func, client) {
    this.func.push( { callback: func, client: client } );
  }
  // Notify the client functions that are registed with the move of this point
  // Each function is called with registered client and with a notification message as the parameter of the function
  // If a function call returns false, the whole notification will return false.
  // The action to the return value is determined by the notifier (the one sends the notification).
  // Normally, the nofifier may void the move and reset the position to the original position before the move.
  this.Notify = function ( msg ) {
    var allowed = true;
    for (var n = 0; n < this.func.length; n++) {
      var f = this.func[n];
      allowed = allowed && f.callback.call( f.client, msg );
    }
    return allowed;
  }
}

// http://math.stackexchange.com/questions/256100/how-can-i-find-the-points-at-which-two-circles-intersect
function Cross2Circles( c1, r1, c2, r2 )
{
  var d = Math.sqrt((c2.X-c1.X)*(c2.X-c1.X)+(c2.Y-c1.Y)*(c2.Y-c1.Y));
  var l = ( r1*r1 - r2*r2 + d*d ) / (d+d);
  var h = Math.sqrt( r1*r1 - l*l );
  
  return [ new Point( ( l / d ) * (c2.X-c1.X) + ( h / d ) * (c2.Y-c1.Y) + c1.X, ( l / d ) * (c2.Y-c1.Y) - ( h / d ) * (c2.X-c1.X) + c1.Y ),
           new Point( ( l / d ) * (c2.X-c1.X) - ( h / d ) * (c2.Y-c1.Y) + c1.X, ( l / d ) * (c2.Y-c1.Y) + ( h / d ) * (c2.X-c1.X) + c1.Y )
         ];
}

function Vector( c, p )
{
	this.center = c;
	this.p = p;

  // arc start point will be set by the edge starting from this vertex
  // arc end point will be set by the edge ending to this vertex
  this.arc = new Arc( this.p, 20 );
  
  this.VectorReCalc = function () {
  	this.dx = p.X - c.X;
    this.dy = p.Y - c.Y;
  	this.distance = Math.sqrt( this.dx*this.dx+this.dy*this.dy );
    var a = this.dx==0 ? 90 : Math.atan( this.dy/this.dx ) * 180 / Math.PI;
    if( this.dx == 0 )
        a = this.dy < 0 ? 270 : 90;
    else if( this.dx < 0 )
      if( this.dy == 0 )
        a = 180;
      else if( this.dy < 0 )
        a += 180; // III 45 + 180 = 225
      else
        a += 180; // II -45 + 180 = 135
    else
      if( this.dy == 0 )
        a = 0;
      else if( this.dy < 0 )
         a += 360; // IV
      // else I 45 = 45 
    this.angle = a;
  	this.dx /= this.distance;
    this.dy /= this.distance;
  }
  
  this.VectorReCalc();
  
  this.AwayFrom1 = function( d, oldPoint ) { // Calculate a point in the line  with a distance from center point
    if( oldPoint && oldPoint instanceof Point ) {
      oldPoint.X = this.center.X+this.dx*d;
      oldPoint.Y = this.center.Y+this.dy*d;
      return oldPoint;
    } else
      return new Point( this.center.X+this.dx*d, this.center.Y+this.dy*d);
  }
  this.SideSign = function (p) { // Calculate the side value for a point to decide the side of the point 
    return (p.Y-this.center.Y)*this.dx - (p.X-this.center.X)*this.dy;
  }
  /*
  this.testdraw1 = function () {
    var cc = document.createElementNS( svgNS, 'circle' );
    cc.setAttribute( 'class', 'circletest' );
    cc.setAttribute( 'cx', this.center.X );
    cc.setAttribute( 'cy', this.center.Y );
    cc.setAttribute( 'r', this.arcRadius );
    svg.appendChild( cc );
  }
  */
  //  this.testdraw1();
}

function Arc( center, radius, color )
{
  // Note: arc always draws clock-wise.
  // The center of an edge arc is on the outside of the polygon. 
  // The center of a vertex arc is on the inside of the polygon, which is the same side of of the center of the polygon.
  // Therefore, edge are draws from v2 to v1.
  // Below, c is the center of the edge arc
  //  1 is the start point of the edge arc and also the start point of v2 arc,
  //  3 is the end point of the edge arc and also the end point of v1 arc.
  //  Edge arc draws from 1, 2, 2, 2, 3.
  //  v1 arc draws a, 2, 3
  //  v2 arc draws 1, 2, b
  //
  //           c       
  //
  //     2 3      1 2
  // v1 a \  2 2 2 / b   v2
  //       \      /
  //        \    /  
  //         \  /
  //          \/
  
  this.center = center;
  this.radius = radius;
  this.color = color;
  this.SetArcPositionAndOrientation = function ( v1, v2 ) {
    // The arc is specified by two vectors. That's enough to calcualte the orientation
    // However, need to calculate the start and end points by calculating the cross points on the circle with the vector
    var v_st = new Vector( this.center, v2.p );
    var v_ed = new Vector( this.center, v1.p );
    var a = v_ed.angle - v_st.angle;
    if( a < 0 ) a += 360;
    this.largeArc = ( a > 180 ? 1 : 0 );
    v2.arc.start = this.start = v_st.AwayFrom1( this.radius ); // start point of the edge arc and the start point of the arc for v2
    v1.arc.end = this.end = v_ed.AwayFrom1( this.radius ); // end point of the edge arc and the end point of the arc for v1
  }
  this.SetArcOrientation  = function () {
    // The start and end point are known already, set by the edge
    // Only need to calcualte the orientation
    var v_st = new Vector( this.center, this.start );
    var v_ed = new Vector( this.center, this.end );
    var a = v_ed.angle - v_st.angle;
    if( a < 0 ) a += 360;
    this.largeArc = ( a > 180 ? 1 : 0 );
  }
  this.CreateView = function (parent, cls) {
      this.view = document.createElementNS( svgNS, 'path' );
      this.view.setAttribute( 'class', cls );
      this.view.setAttribute( 'stroke', this.color );
      parent.appendChild( this.view );
  };
  this.Draw = function () {
      // draw the edge arc from start to end point, clockwise
      var pathD = "M"+this.start.X+","+this.start.Y+" "; //    <path d="M40,20 A10,10 0 0,1 38,25" style="fill:none;stroke:lightblue" />
      pathD += "A"+ this.radius+","+this.radius+" ";
      pathD += "0 ";
      pathD += this.largeArc; // large 1 or small 0 arc
      pathD += ",1 "; // clockwise
      pathD += this.end.X+","+this.end.Y;
      this.view.setAttribute( 'd', pathD );
  }
}

// An edge is specified by 2 vectors and is v2 - v1
function Edge( v1, v2, color )
{
  Vector.call( this, v1.p, v2.p, color );
  this.v1 = v1;
  this.v2 = v2;
  
  this.EdgeReCalc = function () {
    // ReCalc the base vector
    this.VectorReCalc();
    this.centerSign = this.SideSign( v1.center );
    var edgeArcRadius = this.distance - v1.arc.radius - v2.arc.radius;
    // make v1.arcRadius+edgeArcRadius + v2.arcRadius+edgeArcRadius > distance
    // Let c be the center point between v1.p and v2.p
    // Let r = v1.arc.radius = v2.arc.radius
    // Let d = the distance between v1.p and v2.p
    // Let h = the distance between the center of the arc and the center point c
    // Let R = edgeArcRadius
    // h^2 + (d/2)^2 = (R+r)^2
    // We need h >= R + a where a is the smallest gap between the arc and the center point c.
    // h^2 = (R+r)^2 - (d/2)^2 = R^2 + 2Rr + r^2 - (d/2)^2
    // Therefore, h^2 = R^2 + 2Rr + r^2 - (d/2)^2 >= (R+a)^2 = R^2 + 2Ra + a^2
    // 2Rr - 2Ra >= a^2 + (d/2)^2 - r^2
    // R >= ( a^2 + (d/2)^2 - r^2 ) / (2r-2a) ( if r > a )
    var D = this.distance / 2;
    var r = v1.arc.radius;
    var a = 3;
    D = a*a + D*D - r*r;
    D /= (r-a)+(r-a);
    if( edgeArcRadius < 5 ) { // if too close
      edgeArcRadius = 5;
    }
    if( edgeArcRadius < D ) {
      edgeArcRadius = D;
    }
    var cross = Cross2Circles( v1.p, v1.arc.radius+edgeArcRadius, v2.p, v2.arc.radius+edgeArcRadius );
    var side = 0;
    // want to choose the cross point that is on the opposite side of the line v1-v2 with the center point 
    var signC = this.SideSign( cross[side] );
    //    if( this.centerSign > 0 && signC > 0 || this.centerSign < 0 && signC < 0 ) {
    if( signC > 0 ) {
      side = 1;
      signC = this.SideSign( cross[side] );
    }
    // figure out the tangent point between the connecting circle and the vertex circle
    this.arc.center = cross[side];
    this.arc.radius = edgeArcRadius;
    this.arc.SetArcPositionAndOrientation( v1, v2 );
  }
  
  this.EdgeReCalc();
}

function Polygon( color, pos )
{ // https://en.wikipedia.org/wiki/Centroid
  this.color = color;
  var vec = [];
  /*
  for (var i = 0; i < arguments.length; i++) {
      if( arguments[i] instanceof Point ) {
        vec.push( arguments[i] );
      }
  }
  */

  this.mode = 0; // default mode, 0: family layout is fixed, 1: family can be moved around the family head, the first member of the person
  this.SetAdjMode  = function(mode) {
    this.mode = mode;
  }

  for (var i = 0; i < pos.length; i++) {
      if( pos[i] instanceof Point ) {
        vec.push( pos[i] );
      }
  }
  
  // Calculate the center of this Polygon.
  // if c is passed in as a parameter, it is set to the calculated center and returned
  // if parameter is passed in as null, a new center point is calculated and returned. ?? Why ? need to revisit later
  this.CalcCenter = function ( c ) {
    var x = 0.0;
    var y = 0.0;
    for (var i = 0; i < vec.length; i++) {
      x += vec[i].X;
      y += vec[i].Y;
    }
    if( c ) {
      c.X = x/vec.length;
      c.Y = y/vec.length;
      return c;
    } else
      return new Point( x/vec.length, y/vec.length );
  }
  
  // figure out the order of vertices by the angle
  this.swapped = true;
  this.SortByAngle = function () {
    for (var i = 0; i < this.order.length-1; i++) {
      var minAngle = this.vector[this.order[i]].angle;
      for (var j = i+1; j < this.order.length; j++) {
        if( this.vector[this.order[j]].angle < minAngle ) {
          minAngle = this.vector[this.order[j]].angle;
          var minId = this.order[j];
          this.order[j] = this.order[i];
          this.order[i] = minId;
          this.swapped = true; // this indicates the edges need to be recalculated.
        }
      }
    }   
  }
  this.AngleOrderChanged = function () {
    for (var i = 0; i < this.order.length-1; i++) {
      var minAngle = this.vector[this.order[i]].angle;
      for (var j = i+1; j < this.order.length; j++) {
        if( this.vector[this.order[j]].angle < minAngle ) {
          return true; // this indicates the angle order has changed.
        }
      }
    }
    return false; // Angle order has not changed.
  }

  this.center = this.CalcCenter();
  this.order = [];
  this.vector = [];
  // Create center to vertex vectors
  for (var i = 0; i < vec.length; i++) {
     var p = vec[i];
     var v = new Vector( this.center, p, this.color );
     this.order.push(i);
     this.vector.push(v);
  }
  this.SortByAngle();
  // create edges, each edge is a vector of V2-V1 where V1 and V2 are center vectors of two vertices.
  // Edges may change due to the move of vertices.
  // Therefore, it has to be determined after vertex angles are sorted.
  
  this.CreateEdges = function () {
    this.edge = [];
    for (var i = 0; i < this.order.length; i++) {
      var j = i + 1;
      if( j==this.order.length ) j = 0;
      var e = new Edge( this.vector[this.order[i]], this.vector[this.order[j]], this.color );
      this.edge.push( e );
    }
  }
  
  this.CreateEdges();

  this.SetSubjects = function () {
    this.subject = [];
    for (var i = 0; i < arguments.length; i++) {
        this.subject.push( arguments[i] );
    }    
  }
  
  /*  
  this.draw1 = function ( parent ) {
    var redraw = this.pol ? true : false;
    // <polygon points="200,10 250,190 160,210" style="fill:lime;stroke:purple;stroke-width:1" />
    if( ! this.pol ) {
      this.pol = document.createElementNS( svgNS, 'polygon' );
      this.label = [];
      this.cc = [];
    }
    this.points = '';
    for (var i = 0; i < this.order.length; i++) {
      var v = this.vector[this.order[i]]
      var pt = v.p;
      this.points += pt.X + ',' + pt.Y + ' ';
      // If there is no subject defined, 
      // label each vertex with the order number of the vertices when they are provided
      // It's not the order they are drawn.
      var labelPos = v.AwayFrom1( v.distance + 5 ); // outside 5
      if( redraw ) {
  		  this.label[i].setAttribute( "x", labelPos.X );
  		  this.label[i].setAttribute( "y", labelPos.Y );
      } else {
        var label = document.createElementNS( svgNS, 'text' );
        label.setAttribute( "class", "vertexlabel");
  		  label.setAttribute( "x", labelPos.X );
  		  label.setAttribute( "y", labelPos.Y );
  		  var tn = document.createTextNode(this.order[i]);
    		label.appendChild(tn);
  		  parent.appendChild( label );
        this.label.push(label);
      }

      if( redraw ) {
        this.cc[i].setAttribute( 'cx', pt.X );
        this.cc[i].setAttribute( 'cy', pt.Y );
      } else {
        var cc = document.createElementNS( svgNS, 'circle' );
        cc.setAttribute( 'class', 'vertex' );
        cc.setAttribute( 'cx', pt.X );
        cc.setAttribute( 'cy', pt.Y );
        parent.appendChild( cc );
        this.cc.push( cc );
      }       

      if( i==0 ) { // mark the starting point
        labelPos = v.AwayFrom1( v.distance - 5 ); // inside 5
        if( redraw ) {
          this.first.setAttribute( 'cx', labelPos.X );
          this.first.setAttribute( 'cy', labelPos.Y );
        } else {
          cc = document.createElementNS( svgNS, 'circle' );
          cc.setAttribute( 'class', 'startpoint' );
          cc.setAttribute( 'cx', labelPos.X );
          cc.setAttribute( 'cy', labelPos.Y );
          parent.appendChild( cc );
          this.first = cc;
        }       
      }      
    }

    this.pol.setAttribute( 'points', this.points );
 		this.pol.setAttribute( "style", "fill:none;stroke:purple;stroke-width:1");
    if( ! redraw )
      parent.appendChild( this.pol );
    // comment starts
    var cc = document.createElementNS( svgNS, 'circle' );
    cc.setAttribute( 'class', 'center' );
    cc.setAttribute( 'cx', this.center.X );
    cc.setAttribute( 'cy', this.center.Y );
    svg.appendChild( cc );
    // comment ends
    if( redraw ) {
      this.centerView.setAttribute( 'cx', this.center.X );
      this.centerView.setAttribute( 'cy', this.center.Y );
    } else {
      cc = document.createElementNS( svgNS, 'circle' );
      cc.setAttribute( 'class', 'center' );
      cc.setAttribute( 'cx', this.center.X );
      cc.setAttribute( 'cy', this.center.Y );
      parent.appendChild( cc );
      this.centerView = cc;
    }
    
  }
  */

  this.DrawEdges = function () {
    for( i=0; i < this.edge.length; i++ ) {
      var arc = this.edge[i].arc;
      var objclass = 'edgearc' + this.color;
      /*
      if( this.subject ) {
        var j = i + 1;
        if( j==this.order.length ) j = 0;
        var thissubject = this.subject[this.order[i]];
        var relasubject = this.subject[this.order[j]];
        objclass = thissubject.Role( relasubject.Name() );
      }
      */
      arc.CreateView( this.parent, objclass || 'edgearc' );
      arc.Draw();
    }
  }
  
  this.DrawVertices = function () {
    for( var i=0; i < this.order.length; i++ ) {
      var arc = this.vector[this.order[i]].arc;
      arc.SetArcOrientation();
      arc.CreateView( this.parent, 'vertexarc'+this.color );
      arc.Draw();
      /*
      var v_st = new Vector( e.p, e.arcEnd );
      var v_ed = new Vector( e.p, n.arcStart );
      var a = v_ed.angle - v_st.angle;
      if( a < 0 ) a += 360;
      pathD = "M"+e.arcEnd.X+","+e.arcEnd.Y+" "; //    <path d="M40,20 A10,10 0 0,1 38,25" style="fill:none;stroke:lightblue" />
      pathD += "A"+ e.v2.arcRadius+","+e.v2.arcRadius+" ";
      pathD += "0 ";
      pathD += (a < 180 ? 0 : 1); // small 0 or large 1 arc depending on the open angle
      pathD += ",1 "; // clockwise
      pathD += n.arcStart.X+","+n.arcStart.Y;
      cc = document.createElementNS( svgNS, 'path' );
      cc.setAttribute( 'class',  );
      cc.setAttribute( 'd', pathD );
      this.parent.appendChild( cc );
      */
    }
  } 

  this.dragging = {
    svgRoot: null,
    on: false,
    mouseOffset: null,
    newpos: new Point( pos.X, pos.Y )
  };
  
  this.DrawCenter = function (m) {
    if( m==1 ) {
      if( this.cc==null) {
        this.cc = document.createElementNS( svgNS, 'circle' );
        this.cc.setAttribute( 'class', 'center' );
        this.cc.setAttribute( 'cx', this.center.X );
        this.cc.setAttribute( 'cy', this.center.Y );
        this.cc.setAttribute( 'r', 20 /*this.arcRadius*/ );
        this.parent.appendChild(this.cc);
        var me = this
        this.cc.addEventListener( "mousedown", (function (evt) {
              var fixedSubjectDetermined = 0;
              me.subject.forEach(function(element) {
                if( element.selected) ++fixedSubjectDetermined;
              });
              if( fixedSubjectDetermined > 0 ) {
                me.dragging.on = true;
                var p = svg.createSVGPoint();
                p.x = evt.clientX;
                p.y = evt.clientY;
                var m = me.cc.getScreenCTM();
                p = p.matrixTransform(m.inverse());
                me.dragging.mouseOffset = new Point( p.x-me.center.X, p.y-me.center.Y );
              }
            }));
        this.cc.addEventListener( "mouseup", (function () {
            if( ! me.dragging.on ) return;
                me.dragging.on = false;
            }));
        this.cc.addEventListener( "mousemove", (function (evt) {
            if( ! me.dragging.on ) return;
                var p = svg.createSVGPoint();
                p.x = evt.clientX;
                p.y = evt.clientY;
                var m = me.cc.getScreenCTM();
                p = p.matrixTransform(m.inverse());
                me.dragging.newpos.X = p.x - me.dragging.mouseOffset.X;
                me.dragging.newpos.Y = p.y - me.dragging.mouseOffset.Y;
                var oldpos = new Point( me.center.X, me.center.Y );            
                me.cc.setAttribute("x", me.dragging.newpos.X );
                me.cc.setAttribute("y", me.dragging.newpos.Y );
                
                p.x = p.x
                // me.pos.X = me.dragging.newpos.X;
                //me.pos.Y = me.dragging.newpos.Y;
                /*
                if( ! me.center.Notify( me ) ) {
                  me.center.X = oldpos.X;
                  me.center.Y = oldpos.Y;
                  me.cc.setAttribute("x", me.center.X );
                  me.cc.setAttribute("y", me.center.Y );
                  //me.pos.Notify( me );
                  me.dragging.on = false;
                }
                */
            }));


      }
    } else {
      if( this.cc != null) {
        this.parent.removeChild(this.cc)
        this.cc = null
      }
    }
  }
  
  // Draw this polygon.
  // 1. Draw all vertices
  // 2. Draw all edges
  // 3. Draw the center of this polygon if in the mode of adjusting layout (mode=1)
  this.Draw = function ( g ) {
    this.parent = svg;
    if( g ) {
      this.parent = document.getElementById( g );
    }
    /* each subject should show only once, so it should not be shown here.
    if( this.subject ) {
      for (var i = 0; i < this.order.length; i++) {
        var s = this.subject[this.order[i]];
        var v = this.vector[this.order[i]];
        s.Draw( parent, v.p );
      }
    }
    */
    this.DrawVertices();
    this.DrawEdges();
    this.DrawCenter( this.mode );
  //    this.draw1( parent );
  }

  // Drawing
  // Redraw the whole Polygon due to a move of vertex or the layout change (center move)
  // Before the redrawing, the positions of all vertices are set already.
  // 1. Recalculate the center of the polygon
  // 2. Based on the new center, recalculate the vector from the center to each vertex
  // 3. Based on the new vectors for all vertices, recalculate each edge
  // 4. Redraw the arcs for each vertex (represented by the vectors)
  // 5. Redraw the arcs for the eadges
  this.Redraw = function ( s ) {
    /*
    for (var i = 0; i < this.subject.length; i++) {
        if( s == this.subject[i] ) {
          alert( "Position moved: "+s.name );
        }
    } 
    */   
    this.center = this.CalcCenter( this.center );
    //    this.draw1( parent );
    for (var i = 0; i < this.order.length; i++)
      this.vector[this.order[i]].VectorReCalc();
    //this.swapped = false;
    //this.SortByAngle();
    // order may have changed.
    if( this.AngleOrderChanged() ) {
      // order changed, edges changed. discard all edges and recreate them
    //      alert( "Angle order changed, not allowed." );
      return false; // This version does not allow order to be changed.
    } else {
      // order has not changed, edges remain, but need to be recalculated.
      //      alert( "Recalc edges" );
      for (var i = 0; i < this.edge.length; i++)
        this.edge[i].EdgeReCalc();
      // now redraw vertex arcs
      for (i=0; i<this.vector.length; i++ ) {
        var arc = this.vector[i].arc; 
        arc.SetArcOrientation();
        arc.Draw();
      }
      for (i=0; i<this.edge.length; i++ ) {
        arc = this.edge[i].arc;
        arc.SetArcOrientation();
        arc.Draw();
      }
    }
    return true;
  }

  // Lastly, register event handler to redraw when a vertex moves.
  // The redraw is called in responding to only one vertex move when the move of a vertex notifies the listeners.  
  // Although the redraw is registered to all the vertices, it normally responds to only one vertex move as only one vertex can move at a time.
  for (var i = 0; i < this.order.length; i++) {
    var px = this.vector[this.order[i]].p;
    px.RegisterEventListener( this.Redraw, this );
  }
}

var subjects = new Map();

function Subject( name, pos, transform )
{
  this.person =  persons.get(name);
  this.name = name;
  this.pos = pos;
  this.selected = false;
  if( transform ) {
    this.transform = transform;
    this.pos.Add( this.transform );
  }
  this.dragging = {
    svgRoot: null,
    on: false,
    mouseOffset: null,
    newpos: new Point( pos.X, pos.Y )
  };
    
  this.Draw = function ( parent ) {
    this.dragging.svgRoot = parent;
    var label = document.createElementNS( svgNS, 'text' );
    label.setAttribute( "class", this.person.sex+this.person.live );
	  label.setAttribute( "x", this.pos.X );
	  label.setAttribute( "y", this.pos.Y );
	  var tn = document.createTextNode(this.person ? this.person.name : '');
		label.appendChild(tn);
    
    var me = this;
	  parent.appendChild( label );
    this.label = label;

    var bb = label.getBBox();
    var anchor = document.createElementNS( svgNS, 'rect' );
    anchor.setAttribute( "class", "anchor");
	  anchor.setAttribute( "x", bb.x );
	  anchor.setAttribute( "y", bb.y );
	  anchor.setAttribute( "width", bb.width );
	  anchor.setAttribute( "height", bb.height );
    //	  anchor.setAttribute( "style", "fill:yellow;opacity:0.5" );
    //	  parent.appendChild( anchor );
    /*
    var anchor = document.createElementNS( svgNS, 'circle' );
    anchor.setAttribute( "class", "anchor");
    anchor.setAttribute( "cx", this.pos.X );
    anchor.setAttribute( "cy", this.pos.Y );
    */
    this.anchor = anchor;
    /*
    anchor.addEventListener( "mouseover", (function () {
         if( ! me.dragging.on ) return;
            me.pos.X = me.dragging.newpos.X;
            me.pos.Y = me.dragging.newpos.Y;
            me.dragging.on = false;
         }));
    anchor.addEventListener( "mouseout", (function () {
         if( ! me.dragging.on ) return;
            me.pos.X = me.dragging.newpos.X;
            me.pos.Y = me.dragging.newpos.Y;
            me.dragging.on = false;
         }));
         */
    // http://www.codedread.com/dragtest.svg
    anchor.addEventListener( "mousedown", (function (evt) {
      if( opmode==1 ) {
        me.selected = !me.selected
        if( me.selected ) {
          me.anchor.setAttribute( "style", "fill:yellow;opacity:0.5" );
        } else {
          me.anchor.setAttribute( "style", "" );
        }
      } else {
            me.dragging.on = true;
            var p = svg.createSVGPoint();
            p.x = evt.clientX;
            p.y = evt.clientY;
            var m = me.anchor.getScreenCTM();
            p = p.matrixTransform(m.inverse());
            me.dragging.mouseOffset = new Point( p.x-me.pos.X, p.y-me.pos.Y );
      }
      }));
    anchor.addEventListener( "mouseup", (function () {
         if( ! me.dragging.on ) return;
            me.dragging.on = false;
            /*
            me.pos.X = me.dragging.newpos.X;
            me.pos.Y = me.dragging.newpos.Y;
            me.pos.Notify( me );
            */
         }));
    anchor.addEventListener( "mousemove", (function (evt) {
        if( ! me.dragging.on ) return;
        if( evt.which != 1) {
          // mouse is not in down position anymore
          me.dragging.on = false;
          return;
        }
        var p = svg.createSVGPoint();
        p.x = evt.clientX;
        p.y = evt.clientY;
        var m = me.anchor.getScreenCTM();
        p = p.matrixTransform(m.inverse());
        me.dragging.newpos.X = p.x - me.dragging.mouseOffset.X;
        me.dragging.newpos.Y = p.y - me.dragging.mouseOffset.Y;
        me.label.setAttribute("x", me.dragging.newpos.X );
        me.label.setAttribute("y", me.dragging.newpos.Y );
        bb = me.label.getBBox();
        me.anchor.setAttribute("x", bb.x );
        me.anchor.setAttribute("y", bb.y );
        
        var oldpos = new Point( me.pos.X, me.pos.Y );            
        me.pos.X = me.dragging.newpos.X;
        me.pos.Y = me.dragging.newpos.Y;
        if( ! me.pos.Notify( me ) ) {
          me.pos.X = oldpos.X;
          me.pos.Y = oldpos.Y;
          me.label.setAttribute("x", me.pos.X );
          me.label.setAttribute("y", me.pos.Y );
          bb = me.label.getBBox();
          me.anchor.setAttribute("x", bb.x );
          me.anchor.setAttribute("y", bb.y );
          me.pos.Notify( me );
          me.dragging.on = false;
        }
         }));
    /* This is not supported by SVG 2. It is proposed by SVG 3.
    anchor.addEventListener( "drag", (function (event) {
            var cX = event.clientX;     // Get the horizontal coordinate
            var cY = event.clientY;     // Get the vertical coordinate
            var coords1 = "client - X: " + cX + ", Y coords: " + cY;
            var sX = event.screenX;
            var sY = event.screenY;
            var coords2 = "screen - X: " + sX + ", Y coords: " + sY;
            alert( me.person.name + " mouse drag @ "+coords1+" "+coords2);
         }));
    */
	  parent.appendChild( anchor );
  };
  this.Name = function () {
    return this.person ? this.person.name : '';
  }
  this.Role = function () {
      return undefined;
  }
  if( subjects.get(this.name) ) {
    alert( "Duplicate subject name : "+this.name );
  }
  subjects.set( this.name, this );
}

function DisplayMembers( treeId )
{
  var tree = document.getElementById( treeId );
  subjects.forEach(function(s) {
    s.Draw(tree); 
  }, this);
}

var stars = new Map();

function StarFamily( treeId, color )
{
  this.treeId = treeId; // first argument is the 
  this.mode = 0;
  var subjectList = [];
  var posList = [];
  var name = ':';
  for (var i = 2; i < arguments.length; i++) {
      var p = subjects.get( arguments[i] );
      subjectList.push( p );
      name += arguments[i]+':';
      posList.push( p.pos );
  }
  this.polygon = new Polygon( color, posList );
  this.polygon.SetSubjects.apply( this.polygon, subjectList );
  this.polygon.SetAdjMode(this.mode);
  this.Draw = function () { this.polygon.Draw(this.treeId); }
  this.SetAdjMode = function(mode) {
    this.mode = mode;
    this.polygon.SetAdjMode( mode )
  }
  stars.set( name, this )
}

function DisplayStarFamilies()
{
  //  var tree = document.getElementById( treeId );
  stars.forEach(function(s) {
    s.Draw(); 
  }, this);
}

function ReportPositions()
{
  //  alert( "Reporting" );
  if( document.getElementById( "tree" ).style.display == 'none') {
    document.getElementById( "tree" ).style.display = '';
    document.getElementById( "reporting" ).style.display = 'none';
    document.getElementById( 'button' ).innerHTML = 'Report adjusted positions';
  } else {
    document.getElementById( "tree" ).style.display = 'none';
    var text = '';
    var transformList = new Map();
    var tn = 1;
    subjects.forEach(function(s) {
      var p = new Point( s.pos, 'clone' );
      if( s.transform )
          p.Sub( s.transform );
      var t = "new Subject('"+s.name+"', new Point( "+Math.round(p.X*100)/100+', '+Math.round(p.Y*100)/100+' )';
      if( s.transform ) {
        var varname = transformList.get( s.transform );
        if( ! varname ) {
          varname = "shift"+(tn++);
          text += "var " +varname + " = new Point("+Math.round(s.transform.X*100)/100+', '+Math.round(s.transform.Y*100)/100+' );\n'
          transformList.set( s.transform, varname );
        }
        t += ", " + varname;
      }
      t += ');\n';
      text += t;
    }, this);
    document.getElementById( "reporting" ).innerHTML = text;
    document.getElementById( "button" ).innerHTML = 'Show Family Tree';
    document.getElementById( "reporting" ).style.display = '';
  }
}

var opmode = 0 // normal

function ToggleFamilyLayoutAdj()
{
  if( document.getElementById( 'button2' ).innerHTML == 'Adjust Family Layout') {
    document.getElementById( 'button2' ).innerHTML = 'Freeze Family Layout';
    opmode = 1
    stars.forEach(function(s) {
      s.SetAdjMode(opmode); 
      s.Draw()
    }, this);
  } else {
    document.getElementById( 'button2' ).innerHTML = 'Adjust Family Layout';
    opmode = 0
    stars.forEach(function(s) {
      s.SetAdjMode(opmode); 
      s.Draw()
    }, this);
  }
}