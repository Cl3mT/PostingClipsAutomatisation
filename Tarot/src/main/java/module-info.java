module tsp.pro3600.cijavafx {
	exports application;

	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core; 
	
	opens application to javafx.graphics, javafx.fxml;
}