<?php

if(!class_exists('Woo_Minecraft_Db')){
	class Woo_Minecraft_Db{
		var $wpdb, $dbTable;
		
		function __construct(){
			global $wpdb;
			$this->wpdb = $wpdb;
			$dbTable = $wpdb->prefix.'_woo_minecraft';
		}
		
	}
}