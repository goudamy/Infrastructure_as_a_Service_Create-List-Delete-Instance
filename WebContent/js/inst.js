//Launch Instance
function lnch_inst() {
	try{
	
		 BootstrapDialog.show({
            title: 'Launch Instances',
            message: function(dialog) {
                var $message = $('<div></div>');
                var pageToLoad = dialog.getData('pageToLoad');
                $message.load(pageToLoad);
                return $message;
            },
            data: {
                'pageToLoad': './html/launch_instance.html'
            },
            buttons: [{
                label: 'Cancel',
                action: function(dialog){
                    dialog.close();
                }
            }, {
                label: 'Launch',
                cssClass: 'btn-primary',
                action: function(dialog) {
                	
                       	
                	if($("#instanceName").val().trim() === "" || $("#count").val() === ""){
                		try{BootstrapDialog.show({
                			type:BootstrapDialog.TYPE_WARNING,
                	        title: 'Warning',
                	        message: 'Please enter all fields marked with (*).'
                	    });}catch(e){console.log("Check Launch params.")}
                	    return;
                		}
                	var instName = $("#instanceName").val();
                	var instCount = $("#count").val();
                	var instFlavor = $("#flavor").val();
                	var imgName = $("#imageName").val();
                	var types = ["m1.nano","m1.micro","m1.tiny","m1.small"];
                	var zone = $("#selectzone").val();
                	var algor = $('#selectalgo').val().trim().toUpperCase();
                	//if(algor === "NONE") algor = null;
                	$.each(types, function(index, type){
	                	if(instFlavor === type.toString()){
	                		
	                		switch(index){
	                			case 0: instFlavor = 42; break;
	                			case 1: instFlavor = 84; break;
	                			case 2: instFlavor = 1; break;
	                			case 3: instFlavor = 2; break;
	                		}
	                	}
                	});
                	try{t.open();}catch(e){console.log(e)}
                	setTimeout(function(){dialog.close(); },2000);
                	$.ajax({
                		   url: 'data',
                		   data: {
                		      type: 'instance',
                		      action: 'launch',
                		      count: instCount,
                		      flavor: instFlavor,
                		      instanceName: instName,
                		      regionName: zone,
                		      imageName: encodeURI(imgName),
                		      algo:algor
                		   },
                		   error: function() {
                		      console.log("Error in Launch Instance.")
                		   },
                		   success: function(data) {
                			   setTimeout(function(){t.close(); },2000);
                		      list_inst();
                		      
                		   },
                		   type: 'POST'
                		});  
                }
            }]
        });

	
	} catch(e)
	{console.log("Issue with loading Launch Instance dialog." + e)
		 
	} finally {
		function algoenable(){
		     
			 setTimeout(function(){
				 if( $("#selectzone").length > 0){
			 $("#selectzone").prop("disabled", false);
				$('#selectalgo').on("change",function() {
					  if($("#selectalgo").val().trim() === "None"){
						  $("#selectzone").prop("disabled", false);
					  } else {
						  $("#selectzone").prop("disabled", true);
					  }
					  
					});  
				
				 
				flavor
				
				$("#flavor").prop("disabled", false);
				$('#flavor').on("change",function() {
					  if($("#flavor").val().trim() === "VMEmulator"){
						 
						  $("#flavor").prop("disabled", true);
						  $("#count").prop("disabled", true);
						  $("#selectzone").prop("disabled", true);
						  $("#selectError").prop("disabled", true);
						  $("#imageName").val("android");
						  $("#imageName").prop("disabled", true);
					  } else {
						  $("#flavor").prop("disabled", false);
						  $("#count").prop("disabled", false);
						  $("#selectzone").prop("disabled", false);
						  $("#selectError").prop("disabled", false);
						  
					  }
					  
					}); 
				 
				 
				 } else {algoenable();}
			 }, 500);
		    
				
			 }
			 algoenable();	
	}
}



//Launch Instance
function delete_inst(instName,hostip) {
	try{
		try{t.open();}catch(e){console.log(e)}
		
                	$.ajax({
                		   url: 'data',
                		   data: {
                		      type: 'instance',
                		      action: 'delete',
                		      host:encodeURI(hostip),
                		      instanceName: instName
                		   },
                		   error: function() {
                		      console.log("Error in Delete Instance.")
                		   },
                		   before: function(){
                			   try{t.open();}catch(e){console.log(e)}
                		   },
                		   success: function(data) {
                			   setTimeout(function(){t.close(); },2000);
                		      list_inst();
                		      
                		   },
                		   type: 'POST'
                		});  
	} catch(e)
	{console.log("Issue with deleting Instance ." + e)}
}


//Loading Img
var t;
loadingpg();
function loadingpg(){
	 
	try{

		t = new BootstrapDialog({
          title: 'Loading Page...Please wait!',
          message:  $('<div></div>').load('./html/loading.html'),
          closable: false,
          closeByBackdrop: false,
          closeByKeyboard: false	
      });

	} catch(e)
	{console.log("Issue with loading Add Image dialog.")}
}


//List Instance
function list_inst() {
	
	$( "#content" ).load( "html/list_instances.html" );
	try{
		t.open();
	}catch(e){
		console.log("Issue with listing the instances.")
		}
	}





