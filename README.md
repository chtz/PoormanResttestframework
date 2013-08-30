PoormanResttestframework
========================

Sample test specification:

	post http://localhost:8787/
	<?xml version="1.0"?>
	<foo>
		<hello>Hi</hello>
		<hello>Hello</hello>
	</foo>
	end
	extract hellos //foo/hello
	assertEquals(2, hellos.length)
