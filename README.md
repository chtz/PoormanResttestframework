PoormanResttestframework
========================

How to use

Sample ./build.gradle

	apply plugin: 'java'
	apply plugin: 'eclipse'
	sourceCompatibility = 1.5
	version = '1.0'
	jar {
	    manifest {
	        attributes 'Implementation-Title': 'Foo', 'Implementation-Version': version
	    }
	}
	repositories {
	    mavenCentral()
	    
	    ivy {
	    	url "https://raw.github.com/chtz/PoormanResttestframeworkRepository/master" 
	    }
	}
	dependencies {
	    testCompile group: 'junit', name: 'junit', version: '4.+'
	    testCompile group: 'ch.furthermore.poorman', name: 'PoormanResttestframework', version: '1.0.1'
	}


Sample ./integrationTests/demo_test.txt

	post http://localhost:8787/
	<?xml version="1.0"?>
	<foo>
		<hello>Hi</hello>
		<hello>Hello</hello>
	</foo>
	end
	extract hellos //foo/hello
	assertEquals(2, hellos.length)
