function SetPersons()
{
	var jz = new Person( '贾政', 'male');
	var wfr = new Person( '王夫人', 'female');
	var jby = new Person( '贾宝玉', 'male');
	var xym = new Person( '薛姨妈', 'female');
	var bcf = new Person( '宝钗父', 'male');
	var xbc = new Person( '薛宝钗', 'female');
	var jds = new Person( '贾代善', 'male');
	var jm = new Person( '贾母', 'female');
}

function SetRelations()
{
	SetRelation( '贾政', 'husband-wife', '王夫人' );
	SetRelation( '贾政', 'father-child', '贾宝玉' );	
	SetRelation( '王夫人', 'mother-child', '贾宝玉' );	
	SetRelation( '薛姨妈', 'mother-child', '薛宝钗' );
	SetRelation( '贾宝玉', 'husband-wife', '薛宝钗' );
	SetRelation( '贾代善', 'husband-wife', '贾母' );
	SetRelation( '贾代善', 'father-child', '贾政' );	
	SetRelation( '贾母', 'mother-child', '贾政' );
}

SetPersons();
SetRelations();

var family1 = new Family( persons.get('贾政'), persons.get('王夫人') );
var family2 = new Family( undefined, persons.get('薛姨妈') );
var family3 = new Family( persons.get('贾宝玉'), persons.get('薛宝钗') );
var family4 = new Family( persons.get('贾代善'), persons.get('贾母') );

var jz = '贾政';

new Subject('贾政', new Point( 30,100 ));
new Subject('王夫人', new Point( 70,100 ));
new Subject('贾宝玉', new Point( 30,160 ));
new Subject('宝钗父', new Point( 100,140 ));
new Subject('薛姨妈', new Point( 191.16,122.95 ));
new Subject('薛宝钗', new Point( 156.85,207.24 ));
new Subject('贾代善', new Point( 30,20 ));
new Subject('贾母', new Point( 143.9,18.97 ));

var jzwfr = new StarFamily( 'tree', 2, '贾政', '王夫人', '贾宝玉' );
var xymfamily = new StarFamily( 'tree', 3, '宝钗父', '薛姨妈', '薛宝钗' );
var bybc = new StarFamily( 'tree', 4, '贾宝玉', '薛宝钗' );
var jdsjm = new StarFamily( 'tree', 5, '贾代善', '贾母', '贾政' );
