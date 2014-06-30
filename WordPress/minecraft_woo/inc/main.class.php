<?php

if(!class_exists('Woo_Minecraft')){
	class Woo_Minecraft{
				
		function Woo_Minecraft(){
			add_action('woocommerce_checkout_process', array(&$this, 'check_player'));
			add_action('woocommerce_order_status_completed', array(&$this, 'finalize_order'));
			add_action('woocommerce_before_checkout_billing_form', array(&$this, 'anotes'));
			add_action('woocommerce_thankyou', array(&$this, 'thanks'));
			add_action('plugins_loaded', array(&$this, 'checkJSON'));
		}
		
		function anotes($c){
			global $woocommerce;
			
			$items = $woocommerce->cart->cart_contents;
			if(!has_commands($items)) return;
			
			?><div id="woo_minecraft"><?php
			woocommerce_form_field('player_id', array(
				'type'        =>	'text',
				'class'       =>	array(),
				'label'       =>	'Player ID:',
				'placeholder' => 'Required Field',
				),
				$c->get_value('player_id'));
			?></div><?php
		}
		
		function checkJSON(){
			
			$json = array();
			
			$method = @$_REQUEST['woo_minecraft'];
			if(empty($method) ) return;
			
			$key=$_REQUEST['key'];
			if(empty($key)){
				$json['status'] = "error";
				$json['msg']    = "Malformed key.";
			}
			
			$key_db = get_option('wm_key');
			if(empty($key_db)){
				$json['status'] = "error";
				$json['msg']    = "Website key nonexistant.";
			}
			
			if($key_db != $key){
				$json['status'] = "error";
				$json['msg']    = "Keys don't match.";
				$json['web']    = $key;
				$json['db']     = $key_db;
			}
				
			if(isset($json['status'])){
				echo json_encode($json);
				die;
			}
			
			global $wpdb;
			
			if($method == "update"){
				$ids = $_REQUEST['players'];
				if(empty($ids)) $return;
				
//				$ids = $_REQUEST['players'];
				$query = "UPDATE `".$wpdb->prefix."woo_minecraft` SET `delivered`=1 WHERE `id` IN($ids)";
				$rs = $wpdb->query($query);
				if(false===$rs){
					// Error
					echo $wpdb->last_error;				
				}elseif(1>$rs){
					// No results
					echo $wpdb->last_query;
				}else{
					echo "true";
				}
			}else{
				$namesArr = explode(',', $_REQUEST['names']);
				if(empty($namesArr)){
					$json['status']	= "false";
				}else{
					foreach($namesArr as $k => $v){
						$namesArr[$k] = '"'.strtolower($v).'"';
					}
					$namesArr = implode(',',$namesArr);
					$results = $wpdb->get_results("SELECT * FROM `".$wpdb->prefix."woo_minecraft` WHERE  `delivered`=0 AND  `player_name` IN ($namesArr)");
					if(empty($results)){
						$json['status']	= "empty";
					}else{
						$json['status'] = "success";
						$json['data']   = $results;
					}
				}
				echo json_encode($json);
			}
			die;
		}
		
		function updateDonations($ids){
		}
		
		function check_player(){
			global $woocommerce, $order;
					
			$playerID = stripslashes_deep($_POST['player_id']);
			
			$items = $woocommerce->cart->cart_contents;
			if(!has_commands($items)) return;
			
			if(empty($_POST['player_id'])){
				$woocommerce->add_error('Player ID must not be left empty.');
			}else{
				$mcacct = wp_remote_get('http://www.minecraft.net/haspaid.jsp?user='.rawurlencode($playerID), array('timeout'=>5));
				$mcacct = $mcacct['body'];
				if($mcacct != 'true'){
					if($mcacct == 'false'){
						$woocommerce->add_error('Invalid Minecraft Account');
					}else{
						$woocommerce->add_error('Cannot communicate with Minecraft.net  Servers may be down.');
					}
				}
			}
		}
		
		function finalize_order($order_id){
			global $wpdb;
			
			$orderData = new WC_Order($order_id);
			$items = $orderData->get_items();
//			wp_die(print_r($items, true));
			$tmpArray = array();
			$playername = get_post_meta($order_id, 'player_id', true);
			foreach($items as $item){
				// Insert into database table
				$x = array();
				$metag = get_post_meta($item['product_id'], 'minecraft_woo_g', true);
				$metav = get_post_meta($item['variation_id'], 'minecraft_woo_v', true);
				if(!empty($metag)){
					for($n=0;$n<$item['qty'];$n++){
						foreach($metag as $command){
							$x = array(
								'postid'      =>	$item['product_id'],
								'command'     =>	$command,
								'orderid'     =>	$order_id,
								'player_name' =>	$playername
							);
							array_push($tmpArray, $x);
						}
					}
				}
				
				if(!empty($metav)){
					for($n=0;$n<$item['qty'];$n++){
						foreach($metav as $command){
							$x1 = array(
								'postid'      =>	$item['variation_id'],
								'command'     =>	$command,
								'orderid'     =>	$order_id,
								'player_name' =>	$playername
							);
							array_push($tmpArray, $x1);
						}
					}
				}
			}
			
			if(!empty($tmpArray)){
				foreach($tmpArray as $row){
					$wpdb->insert( $wpdb->prefix."woo_minecraft",$row,array('%d','%s','%d','%s') );
				}
			}
			//wp_die($wpdb->last_query);
		}
		
		function thanks($id){
			$playername = get_post_meta($id, 'player_id', true);
			if(!empty($playername)){
				?><div class="woo_minecraft"><h4>Minecraft Details</h4><p><strong>Username: </strong><?php $playername ?></p></div><?php
			}
		}
	}
}