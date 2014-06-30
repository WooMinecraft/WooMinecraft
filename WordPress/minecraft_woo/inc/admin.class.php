<?php
if(!class_exists('Woo_Minecraft_Admin')){
	class Woo_Minecraft_Admin{
		function Woo_Minecraft_Admin(){
			add_action('admin_enqueue_scripts', array(&$this, 'scripts'));
			
			add_action('woocommerce_checkout_update_order_meta', array(&$this, 'update_order_meta'));
			add_action('woocommerce_admin_order_data_after_billing_address',array(&$this, 'display_player_name_in_order_meta'));
			add_action('woocommerce_product_options_general_product_data', array(&$this,'add_g_field'));
			add_action('woocommerce_process_product_meta', array(&$this,'save_g_field'));
			
			add_action('woocommerce_product_after_variable_attributes', array(&$this, 'add_v_field'), 10, 2);
			add_action('woocommerce_product_after_variable_attributes_js', array(&$this, 'add_v_field_js'));
			add_action('woocommerce_process_product_meta_variable', array(&$this, 'variable_fields_process'));
			
			add_action('woocommerce_order_status_changed', array(&$this, 'delete_sql_data'), 10, 3);
			
			//add_action( 'woocommerce_order_item_line_item_html', array(&$this, 'line_item'), 10, 2);
			
			add_action('admin_menu', array(&$this, 'setupAdminMenu'));
			add_action('admin_init', array(&$this, 'admininit'));
		}
		
		function add_g_field(){
			global $post;
			$meta = get_post_meta($post->ID, 'minecraft_woo_g', true);
			
		?>
        	<div class="woo_minecraft">
            	<p class="title">Minecraft WooCommerce</p>
            	<p class="form-field woo_minecraft">
                	<label for="woo_minecraft_general">Commands</label>
                    <input type="button" class="button button-primary woo_minecraft_add" name="Add" id="woo_minecraft_add" value="Add" />
                    <input type="button" class="button woo_minecraft_reset" name="Reset" id="woo_minecraft_reset" value="Reset Fields" />
                    <img class="help_tip" data-tip="Any commands added here, will run on top of variable commands if any. <br /><br />No leading slash is needed." src="<?=plugins_url( "help.png", dirname(__FILE__) );?>" height="16" width="16" />
                    <span class="woo_minecraft_copyme" style="display:none">
                        <input type="text" name="minecraft_woo[general][]" value="" class="short" placeholder="Use %s for player name"/>
                        <input type="button" class="button button-small delete remove_row" value="Delete">
                    </span>
                    <?php if(!empty($meta)): ?>
						<?php foreach($meta as $command): ?>                	
                            <span>
                                <input type="text" name="minecraft_woo[general][]" value="<?= $command; ?>" class="short"/>
                                <input type="button" class="button button-small delete remove_row" value="Delete">
                            </span>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </p>
            </div>
        <?php
		}
		
		function add_v_field($l, $v){
			global $woocommerce;
			$meta = get_post_meta($v['variation_post_id'], 'minecraft_woo_v', true);
			?>
            <tr><td>
			<div class="woo_minecraft_v">
				<p class="title">Minecraft WooCommerce</p>
                <p class="form-field woo_minecraft woo_minecraft_v">
                	<label>Commands</label>
                    <input type="button" class="button button-primary woo_minecraft_add" name="Add" id="woo_minecraft_add_v" value="Add" />
                    <input type="button" class="button woo_minecraft_reset" name="Reset" id="woo_minecraft_reset_v" value="Reset Fields" />
                    <img class="help_tip" data-tip="Use %s for the player's name.<br /><br />No leading slash is needed." src="<?= plugins_url( "help.png", dirname(__FILE__) ) ?>" height="16" width="16" />
                    <span class="woo_minecraft_copyme" style="display:none">
                        <input type="text" name="minecraft_woo[variable][<?=$l; ?>][]" value="" class="short" placeholder="Use %s for player name"/>
                        <input type="button" class="button button-small delete remove_row" value="Delete">
                    </span>
                    <?php if(!empty($meta)): ?>
						<?php foreach($meta as $command): ?>
                            <span>
                                <input type="text" name="minecraft_woo[variable][<?=$l;?>][]" value="<?= $command; ?>" class="short"/>
                                <input type="button" class="button button-small delete remove_row" value="Delete">
                            </span>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </p>
			</div>
            </tr></td>
            <?php
		}
		
		function add_v_field_js(){
			global $woocommerce;
			?>
            <tr><td>\
			<div class="woo_minecraft_v">\
				<p class="title">Minecraft WooCommerce</p>\
                <p class="form-field woo_minecraft woo_minecraft_v">\
                	<label>Commands</label>\
                    <input type="button" class="button button-primary woo_minecraft_add" name="Add" id="woo_minecraft_add_v" value="Add" />\
                    <input type="button" class="button woo_minecraft_reset" name="Reset" id="woo_minecraft_reset_v" value="Reset Fields" />\
                    <img class="help_tip" data-tip="Any commands added here, will run on top of variable commands if any. <br /><br />No leading slash is needed." src="<?= plugins_url( "help.png", dirname(__FILE__) ) ?>" height="16" width="16" />\
                    <span class="woo_minecraft_copyme" style="display:none">\
                        <input type="text" name="minecraft_woo[variable]['+loop+'][]" value="" class="short" placeholder="Use %s for player name"/>\
                        <input type="button" class="button button-small delete remove_row" value="Delete">\
                    </span>\
                </p>\
			</div>\
            </tr></td>\
            <?php
		}
		
		function delete_sql_data($order_id, $curstatus, $newstatus){
			if($curstatus != 'completed') return;
			global $wpdb;
			
			$orderData = new WC_Order($order_id);
			$items = $orderData->get_items();
			$tmpArray = array();
			$playername = get_post_meta($order_id, 'player_id', true);
			$result = $wpdb->delete($wpdb->prefix."woo_minecraft", array('orderid'=>$order_id), array('%d'));
			if(FALSE===$result){
				wp_die($wpdb->last_error);
			}
			
		}
		
		
		function display_player_name_in_order_meta($order){
			$playerID = get_post_meta($order->id, 'player_id', true) or "N/A";
			wp_nonce_field("woominecraft", "woo_minecraft_nonce");
			?>
            	
                <p><strong>Player Name:</strong> <?=$playerID; ?></p>
            <?php if($playerID != "N/A") : ?>
            	<?php global $post; ?>
                <p><input type="button" class="button button-primary" id="resendDonations" value="Resend Donations" data-id="<?=$playerID;?>" data-orderid="<?=$post->ID;?>"/>
            <?php endif;
		}
		
		function install(){
			global $wp_version, $wpdb; 
			if(version_compare($wp_version, '3.0', '<')){
				die('<div class="error"><strong>ERROR: </strong> Plugin requires WordPress v3.1 or higher.</div>');
			}
			
		
			
			$tName = $wpdb->prefix."woo_minecraft";
			$table = "CREATE TABLE IF NOT EXISTS ".$tName." (
				id mediumint(9) NOT NULL AUTO_INCREMENT,
				orderid mediumint(9) NOT NULL,
				postid mediumint(9) NOT NULL,
				delivered TINYINT(1) NOT NULL DEFAULT 0,
				player_name VARCHAR(64) NOT NULL,
				command VARCHAR(128) NOT NULL,
				PRIMARY KEY  (id)
			);";
			require_once(ABSPATH.'wp-admin/includes/upgrade.php');
			dbDelta($table);
		}
		
		function line_item($item_id, $item){
			global $post;
			$meta_v = get_post_meta($item['variation_id'], 'minecraft_woo_v');
			print_r($item['variation_id']);	
			if(!empty($meta_v)){
				?>
                	<span class="woominecraft resend_item">
                    		<button class="button button-primary wooitemresend" data-orderid="<?=$post->ID?>" data-variation="<?=$item['variation_id']?>"><span>Resend Donation</span></button>
                    </span>
                <?php
			}
			
			
		}
		
		function save_g_field($postid){
			$field = $_POST['minecraft_woo']['general'];
			if(isset($field) && !empty($field)){
				update_post_meta($postid, 'minecraft_woo_g', array_filter($_POST['minecraft_woo']['general']));
			}
		}
		
		function scripts(){
			wp_register_script('woo_minecraft_js', plugins_url('jquery.woo.js',dirname(__FILE__) ), array('jquery'), '1.0', true);
			wp_register_style('woo_minecraft_css', plugins_url('style.css', dirname(__FILE__)), false, '1.0');
			wp_enqueue_script('woo_minecraft_js');
			wp_enqueue_style('woo_minecraft_css');
		}
		
		
		function setupAdminPage(){
			?>
            	<div class="wrap">
                	<h2>Woo Minecraft Options</h2>
                    <form method="post" action="options.php">
                    <?php settings_fields('woo_minecraft'); ?>
                    <table class="form-table wide-fat">
                    	<tbody>
                            <tr>
                                <th><label for="wm_key">Game Key</label></th>
                                <td><input type="text" name="wm_key" id="wm_key" value="<?= get_option('wm_key'); ?>"/>
                                	<p class="description">Type /woo register in-game as op to get your key.</td>
                            </tr>
                        </tbody>
                    </table>
                    <?php submit_button(); ?>
                    </form>
                </div>
            <?php
		}
		
		function setupAdminMenu(){
			add_options_page('Woo Minecraft', 'Woo Minecraft', 'manage_options', 'woominecraft', array(&$this, 'setupAdminPage'));
		}
		
		function admininit(){
			register_setting('woo_minecraft', 'wm_key');
//			register_setting("");
		}
		
		function uninstall(){
			global $wpdb;
			$query = "DROP TABLE IF EXISTS ".$wpdb->prefix."woo_minecraft";
			$wpdb->query($query);
		}
		
		function update_order_meta($order_id){
			if($_POST['player_id']) update_post_meta($order_id, 'player_id', esc_attr($_POST['player_id']));
		}
		
		function variable_fields_process($post_id){
			$variable_sku = $_POST['variable_sku'];
			$variable_post_id = $_POST['variable_post_id'];
			$woo_minecraft = $_POST['minecraft_woo']['variable'];
			for($i=0;$i<sizeof($variable_sku);$i++){
				$variation_id=(int)$variable_post_id[$i];
				if(isset($woo_minecraft[$i])){
					update_post_meta($variation_id, 'minecraft_woo_v', array_filter($woo_minecraft[$i]));
				}
			}
		}
	}
}