$(document).ready(function() {
	var $form = $('#reseller-search-form');
	
	$('#query-filters').find('a').click(function(e) {
		e.preventDefault();
		if ($(this).parent('li').is('.selected')) {
			$(this).parent('li').removeClass('selected');
			$form.find('input[name="filter"]').val('');
		} else {
			$form.find('input[name="filter"]').val($(this).attr('data-val'));

		}
		$form.find("#search").click();
	});
	
	$form.find('#cancel').click(function(e) {
		e.preventDefault();
		$form.find(':text,:hidden,:selected').val('');
		$form.find("#search").click();
	});
	
	//multi-delete btn
	$('.multi-delete-btn').click(function(e){
		e.preventDefault();
		var dataUrl="{0}/supplier/{1}/resellers/{2}.json";
		var supplierId = $form.find('input[name="supplierId"]').val();
		if(confirm("Delete this items?")){
			var text="";
			$form.find("input[name='item-checkbox']").each(function(){
				if(this.checked==true){
					text += $(this).val() +",";
				}
			});
			if(text==""){
				alert("Please select a item!");
				return false;
			}else{
				$.ajax({
					   url:dataUrl.format(basePath,supplierId,text.substring(0, text.length - 1)),
					   type: 'DELETE',
					   success: function( data ) {
						   if(data && data.result.success){
							   window.location.reload();
							}
					   }
				});
			}
			return true;
		};
		
	});
	
//	$form.find("input[name='all-checkbox']").click(function(){
//		if(this.checked==true){
//			$form.find("input[name='item-checkbox']").each(function(){
//				$(this).attr("checked",true);
//	        });
//	    }else{
//	    	$form.find("input[name='item-checkbox']").each(function(){
//	    		$(this).attr("checked",false);
//	         });
//	    }
//	});
});

