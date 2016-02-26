import groovy.sql.Sql

def classLoader = this.getClass().getClassLoader();
while (!classLoader.getClass().getName().equals("org.codehaus.groovy.tools.RootLoader")) 
{
  classLoader = classLoader.getParent()
}

groovy.grape.Grape.grab(group:'com.oracle', module:'ojdbc5', version:'[11.2.0.3.0,)', classLoader: classLoader)

class SqlTool
{
  private sql;
 
  public SqlTool(url, driver, username, password) 
  {
    this.sql = Sql.newInstance(url, username, password, driver)
    this.sql.eachRow('''
        SELECT OBJECT_NAME,
			   OBJECT_TYPE,
			   DECODE(T.OBJECT_TYPE, 'PACKAGE', 'pks', 'PACKAGE BODY', 'pkb', 'TYPE', 'type', 'FUNCTION', 'fun', 'TRIGGER', 'trg', 'PROCEDURE', 'proc', 'TABLE', 'tab', 'INDEX', 'idx', 'VIEW', 'view', 'SYNONYM', 'syn', 'SEQUENCE', 'seq', OBJECT_TYPE) EXT,
			   DBMS_METADATA.GET_DDL(DECODE(T.OBJECT_TYPE, 'PACKAGE', 'PACKAGE_SPEC', 'PACKAGE BODY', 'PACKAGE_BODY', T.OBJECT_TYPE), OBJECT_NAME) DDL
		  FROM USER_OBJECTS T
		 WHERE T.OBJECT_TYPE <> 'LOB'
	''')
	{row ->
		def filename = "${row.object_name}.${row.ext}"
		File file = new File(filename)
		file << row.ddl.getCharacterStream().getText()
		println filename
    }
  }
}
 
new SqlTool("jdbc:oracle:thin:@localhost:1521:sid", "oracle.jdbc.driver.OracleDriver", "scott", "tiger")