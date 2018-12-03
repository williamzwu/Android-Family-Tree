// This file has to be saved in unicode
var svgNS = "http://www.w3.org/2000/svg";

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
	this.Transform = function( matrix ) {
		// http://www.w3.org/TR/SVGTiny12/coords.html#TransformMatrixDefined
		return new Point( (matrix.a * this.X) + (matrix.c * this.Y) + matrix.e, (matrix.b * this.X) + (matrix.d * this.Y) + matrix.f );
	};
}

function Distance( a, b ) {
	return Math.sqrt( (b.X-a.X)*(b.X-a.X)+(b.Y-a.Y)*(b.Y-a.Y));
}

var persons = new Map();

function Person( _name, _sex, _live )
{
	this.name = _name;
	this.sex = _sex;
	this.live = _live==null?'':_live
	persons.set( this.name, this );
	this.role = new Map();
	this.AddRole = function ( familyName, role ) { // role: father, mother, 1, 2, 3 for children in order
		this.role.set( familyName, role );
	};
}

var validRelations = [
	 'husband-wife',
	 'father-child',
	 'mother-child',
	 'sibling-elder'
	 ];

function SetRelation( name1, rel, name2 )
{
	var p1 = persons.get( name1 );
	var p2 = persons.get( name2 );
	switch ( rel ) {
	case 'husband-wife':
		p1.wife = name2;
		p2.husband = name1;
		break;
	case 'father-child':
		if( p1.children == undefined || ! (p1.children instanceof Array) ) {
			p1.children = [];
		}
		p1.children.push( name2 );
		p2.father = name1;
		break;
	case 'mother-child':
		if( p1.children == undefined || ! (p1.children instanceof Array) ) {
			p1.children = [];
		}
		p1.children.push( name2 );
		p2.mother = name1;
		break;
	case 'sibling-elder':
		p1.younger = name2;
		p2.elder = name1;
		break;
	}
}

var families = new Map();
	 
function Family( father, mother )
{
	this.father = father;
	this.mother = mother;
	this.name = '';
	if( father ) {
		this.name += ":F:"+father.name;
	}
	if( mother ) {
		this.name += ":M:"+mother.name;
	}
	if( father ) father.AddRole( this.name, 'father' );
	if( mother ) mother.AddRole( this.name, 'mother' );
	if( father && mother ) {
		this.childList = [];
		for (var index = 0; mother.children && index < mother.children.length; index++) {
			var childName = this.mother.children[index];
			var child = persons.get( childName );
			if( child.father == father.name ) {
				child.AddRole( this.name, this.childList.length );
				this.childList.push( child );
			}
		}
	}
	if( father && ! mother ) {
		this.childList = [];
		for (var index = 0; father.children && index < father.children.length; index++) {
			childName = father.children[index];
			child = persons.get( childName );
			child.AddRole( this.name, this.childList.length );
			this.childList.push( child );
		}
	}
	if( ! father && mother ) {
		this.childList = [];
		for (var index = 0; mother.children && index < mother.children.length; index++) {
			childName = mother.children[index];
			child = persons.get( childName );
			child.AddRole( this.name, this.childList.length );
			this.childList.push( child );
		}
	}
	families.set( this.name, this );
	this.SetPanel = function ( panel ) {
		this.panel = panel;
	};
}

// Case 1: Given father and/or mother and panel position
// Case 2: Given father and/or mother positions
// Case 3: Given one of the child's position
// Case 4: Given two children' positions
// Case A: Main family, given absolute position of the box (x, y)

function FamilyPanel( family, loc, size )
{
	this.family = family;
	this.translate = loc;
	this.xMargin = 30;
	this.yTopMargin = 20; // The margin from a top y of a text element position. This is because the y of text is at the bottom. 
	this.yBotMargin = 10; // The margin from a bottom y of a text element position
	this.size = size ? new Point( size, 'clone') : new Point( 100, 100 );
	this.nameWidth = 36;
	this.nameHeight = 15;
	this.nameHeightMargin = 10;
	this.nameWidthMargin = 10;
	// x
	// xMargin  + 1/2 father + nameWidthMargin + 1/2 mother + xMargin
	//   30         20         10             20        30
	// y 2- children
	// yTopMargin + (Parent)nameHeight + nameHeightMargin + (child)nameHeight + yBotMargin
	// 20         +        15          +     10           +        15         +     10      = 70
	// y 4- children
	// yTopMargin + (Parent)nameHeight + nameHeightMargin + (child)nameHeight + nameHeightMargin + (child)nameHeight + yBotMargin
	// 20         +        15          +     10           +        15         +     10           +        15         +     10      = 95
	this.Transform = function ( person, pos, matrix ) {
		if( person.location )
			return;
		person.location = matrix ? pos.Transform( matrix ) : new Point( pos, 'clone' );
	};
	// Calculate children' positions
	this.childLevel = Math.round( 0.1+family.childList ? family.childList.length / 2 : 0 );
	if( this.childLevel > 2 ) {
		this.size.Y += (this.childLevel-2)*(this.nameHeight + this.nameHeightMargin)
	}
	this.firstChildPosY = this.size.Y - (this.childLevel-1)*(this.nameHeightMargin+this.nameHeight) - this.yBotMargin;
	this.position = {
		father: new Point( this.xMargin, this.yTopMargin),
		mother: new Point( this.size.X - this.xMargin, this.yTopMargin ),
		child: new Array(this.family.childList.length)
	  };
	  
	this.SetWidth = function ( w ) {
		if( this.family.mother ) this.position.mother.X += w - this.size.X;
		for (var index = 1; index < this.position.child.length; index += 2 )
			this.position.child[index].X += w - this.size.X;
		this.size.X = w;
	}
	this.SetChildrenPos = function() {
		var posY = this.firstChildPosY;
		for (var index = 0; index < this.position.child.length; index += 2 ) {
			this.position.child[index] = new Point( this.position.father.X, posY );
			if( index+1 < this.position.child.length )
				this.position.child[index+1] = new Point( this.position.mother.X, posY );
			posY += this.nameHeightMargin+this.nameHeight;
		}
	}
	this.transformText = function() {
		var transformTxt = "";
		if( this.translate && ( this.translate.X != 0 || this.translate.Y != 0 ) )
			transformTxt += "translate("+this.translate.X+","+this.translate.Y+") ";
		if( this.skewYAngle && this.skewYAngle != 0 )
			transformTxt += "skewY("+this.skewYAngle+") ";
		if( this.cos && this.cos != 1 )
	  		transformTxt += " scale("+this.cos+",1)";
		if( this.moveBack && (this.moveBack.X != 0 || this.moveBack.Y != 0 ) )
	  		transformTxt += " translate("+this.moveBack.X+","+this.moveBack.Y+")";
		if( transformTxt == "")
			transformTxt = undefined;
		return transformTxt;
	}
}

// Case 1: Given father and/or mother and panel position.
// This is a main panel to decide other family panels' positions.
// Because it's a main panel, the position p will determine all the relative and absolute positions.
function MainPanel( family, location, size )
{
	FamilyPanel.call( this, family, location, size ); // Create the base object
	family.SetPanel( this );
	this.SetChildrenPos();
}

// Case 2: Given father and/or mother positions
function ParentPanel( family )
{
	FamilyPanel.call( this, family ); // Create the base object
	family.SetPanel( this );
	this.SetChildrenPos();
	if( family.father.location && ! family.mother.location ) {
		// single father, use father's position to determine panel location
		this.translate = new Point( family.father.location, 'clone' );
	 	this.translate.Sub( this.position.father );
	} else if( ! family.father.location && family.mother.location ) {
		// single mother, use mother's position to determine panel location
		this.translate = new Point( family.mother.location, 'clone' );
	 	this.translate.Shift( this.size.X-this.position.mother.X, -this.position.mother.Y );
	} else {
		this.translate = new Point( family.father.location, 'clone' );
	 	this.translate.Sub( this.position.father );
	    this.distance = Distance( family.father.location, family.mother.location );
		// Our goal is to skew the panel to match the angle of the father-mother line.
		// and at the same time, keep the absolute location of both father and mother.
		// Before skewing it, we set the X-axis distance to the real distance between father and mother
		// because this is the distance when we look at the panel from straight angle.
		// So the panel's width is the distance plus the margin (both left and right)
		this.SetWidth(this.xMargin + this.distance + this.xMargin);
		// The angle can be calcualated by the cosin of that angle
	  	this.cos = (family.mother.location.X-family.father.location.X) / this.distance;
		this.tan = (family.mother.location.Y-family.father.location.Y) / (family.mother.location.X-family.father.location.X);
	  	this.skewYAngle = Math.atan( this.tan ) * 180 / Math.PI;
		// After skewing it in Y, the distance is enlarged by 1/cos(a) where a is the skew angle.
		// Therefore, we need scale it back by cos(a) to maintain the distance.
		// However, another issue arises, the X of father is also scaled back, we need to move it back to the original position
		// i.e. the difference between the current positon and the original position = (this.position.father.X - this.position.father.X * cos)
		// For Y, the skew move downs father's Y by X*tg(a), so we need to move up by the same.
	  	this.moveBack = new Point( this.position.father.X * (1-this.cos),
			  -this.xMargin*(family.mother.location.Y-family.father.location.Y)/(family.mother.location.X-family.father.location.X) );
		// When father-mother line is horizontal, i.e. family.father.location.Y=family.mother.location.Y,
		// cos = 1, skewYAngle = 0, moveBack=(0,0)
	}
}

// Case 3, Given a child's position
function ChildPanel( family, child )
{
	FamilyPanel.call( this, family ); // Create the base object
	family.SetPanel( this );
	this.SetChildrenPos();
	
	for (var cn = 0; family.childList && cn < family.childList.length; cn++) {
		var ch = family.childList[cn];
		if( ch.name == child.name )
			break;
	}
	var thispos = this.position.child[cn];
	this.translate = child.location.Sub( thispos );
}

function DisplayPanel()
{
	//
	var g = document.createElementNS( svgNS, 'g' );
	g.setAttributeNS( null, "id", this.family.name );
	g.setAttributeNS( null, "style", "opacity:0.5");
	
	var matrix;
	var transform = this.transformText();
	if( transform ) {
		g.setAttributeNS( null, "transform", transform );
	    matrix = g.getScreenCTM();
	}
	
	var rect = document.createElementNS( svgNS, 'rect' );
	rect.setAttributeNS( null, "class", "icon_frame");
	rect.setAttributeNS( null, "x", "0");
	rect.setAttributeNS( null, "y", "0");
	rect.setAttributeNS( null, "width", this.size.X );
	rect.setAttributeNS( null, "height", this.size.Y );
	rect.setAttributeNS( null, "fill", "url(#icon_background)" );
	rect.setAttributeNS( null, "stroke", "#000000" );
	rect.setAttributeNS( null, "fill-opacity", "0.7" );
	g.appendChild( rect );
	
	var txt;
	var tn;
	
	if( this.family.father ) {
		txt = document.createElementNS( svgNS, 'text' );
		txt.setAttribute( "class", "father");
		txt.setAttribute( "x", this.position.father.X );
		txt.setAttribute( "y", this.position.father.Y );
		txt.setAttribute( "comp-op", "soft-light");
		tn = document.createTextNode(this.family.father.name);
		txt.appendChild(tn);
		g.appendChild( txt );
		this.Transform( this.family.father, this.position.father, matrix );
	}
	
	if( this.family.mother ) {
		txt = document.createElementNS( svgNS, 'text' );
		txt.setAttribute( "class", "mother");
		txt.setAttribute( "x", this.position.mother.X );
		txt.setAttribute( "y", this.position.mother.Y );
		txt.setAttribute( "comp-op", "soft-light");
		tn = document.createTextNode(this.family.mother.name);
		txt.appendChild(tn);
		g.appendChild( txt );
		this.Transform( this.family.mother, this.position.mother, matrix );
	}

	for (var c = 0; c < this.family.childList.length; c++) {
		var child = this.family.childList[c];
		txt = document.createElementNS( svgNS, 'text' );
		txt.setAttribute( "class", child.sex=='male'?'son':'daughter');
		txt.setAttribute( "x", this.position.child[c].X );
		txt.setAttribute( "y", this.position.child[c].Y );
		txt.setAttribute( "comp-op", "soft-light");
		tn = document.createTextNode(child.name);
		txt.appendChild(tn);
		g.appendChild( txt );
		this.Transform( child, this.position.child[c], matrix );
	}
	var svg = document.getElementById( 'myroot' );
	svg.appendChild( g );
}

