import groovy.sql.Sql

def classLoader = this.getClass().getClassLoader();
while (!classLoader.getClass().getName().equals("org.codehaus.groovy.tools.RootLoader")) {
  classLoader = classLoader.getParent()
}

groovy.grape.Grape.grab(group:'com.oracle', module:'ojdbc5', version:'[11.2.0.3.0,)', classLoader: classLoader)

class SqlTool {
  private sql;
 
  public SqlTool(url, driver, username, password) {
    this.sql = Sql.newInstance(url, username, password, driver)
    this.sql.eachRow('''
			select 
				object_name, 
				object_type, 
				decode(
					t.object_type, 
					'PACKAGE', 'pks', 
					'PACKAGE BODY', 'pkb',
					object_name) ext, 
				dbms_metadata.get_ddl(
					decode(
						t.object_type, 
						'PACKAGE', 'PACKAGE_SPEC', 
						'PACKAGE BODY', 'PACKAGE_BODY'
					),object_name) ddl 
			from 
				user_objects t 
			where 
				t.object_type in ('PACKAGE', 'PACKAGE BODY')
				'''){
		row ->
		def filename = "${row.object_name}.${row.ext}"
		File file = new File(filename)
		file << row.ddl.getCharacterStream().getText()
		println filename
	}
  }
}
 
new SqlTool("jdbc:oracle:thin:@localhost:1521:sid", "oracle.jdbc.driver.OracleDriver", "scott", "tiger")