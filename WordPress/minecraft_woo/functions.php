<?
/*
	Plugin Name: Minecraft WooCommerce
	Plugin URI: http://plugish.com/plugins/minecraft_woo
	Description: None yet
	Author: Jerry Wood
	Version: 0.01
	Author URI: http://plugish.com
*/

include 'inc/admin.class.php';
include 'inc/main.class.php';

function has_commands($data){
	if(is_array($data)){
		// Assume $data is cart contents
		foreach($data as $item){
			$metag = get_post_meta($item['product_id'], 'minecraft_woo_g', true);
			$metav = get_post_meta($item['variation_id'], 'minecraft_woo_v', true);
			if(empty($metag) && empty($metav)){
				continue;
			}else{
				return true;
			}
		}
	}else{
		
	}
	
	return false;
}

new Woo_Minecraft_Admin;	
register_activation_hook(__FILE__, array('Woo_Minecraft_Admin', 'install')); 
register_uninstall_hook(__FILE__, array('Woo_Minecraft_Admin', 'uninstall'));
new Woo_Minecraft;