// JavaScript Document
jQuery(document).ready(function($) {
	var deleteRow = $('.remove_row');
	
	var wpNonce = $('#woo_minecraft_nonce').val();
	
	
	$('.woo_minecraft_add').live("click", function(e){
		// Add a row
		var curblock = $(this).parent();
		var x = curblock.find('.woo_minecraft_copyme').clone().removeClass('woo_minecraft_copyme').removeAttr('style');
		curblock.append(x);
	});
	
	$('.woo_minecraft_reset').live("click", function(e){
		// Reset all rows (delete them all)
		var x = confirm('This will delete ALL commands, are you sure?  This cannot be undone.');
		var curblock = $(this).parent();
		if(x){
			curblock.find('span').not('.woo_minecraft_copyme').fadeOut(200, function(e){
				$(this).remove();
			});
		}
	});
	
	deleteRow.live("click",function(e){
		$(this).parent('span').fadeOut(200, function(e){
			$(this).remove();
		});
		
	});
	
	
    $('#resendDonations').click(function(e){
		// Handle ajax
		var postData = {
			'woo_minecraft_nonce': wpNonce,
			'action':'mc_resend',
			'method':'order',
			'order':$(this).attr('data-orderid'),
			'player':$(this).attr('data-id')
		};
		
	});
	
	// Jquery hack for per-item resends
	$('.woominecraft').find('.resend_item').each(function(e){
		var table = $(this).parent().find('table.woocommerce_order_items');
		table.find('table tfoot button').after($(this));
	});
	
	$('.wooitemresend').live('click', function(e){
		e.preventDefault();
		var postData = {
			'woo_minecraft_nonce': wpNonce,
			'action':'mc_resend',
			'method':'single',
			'order':$(this).attr('data-orderid'),
			'variation':$(this).attr('data-variation')
		};
	});
	
});