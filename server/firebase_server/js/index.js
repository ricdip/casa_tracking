window.onload = loadPhones();

function loadPhones() {
    receive();
}

function receive(){

	var str = 'get_phones';

	if (window.XMLHttpRequest) {
    // code for modern browsers
    // IE7+, Firefox, Chrome, Opera, Safari
    	xmlhttp = new XMLHttpRequest();
 	} else {
    // code for old IE browsers
    // IE6, IE5
   		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}

	xmlhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
           
			var response = JSON.parse(this.responseText);

			if( !( (response == false) || (response == "false") ) ){
				fillSelect(response);
			}
			else {
				document.getElementById("resultParagraph").innerHTML = "RESULT : " + this.responseText;
				document.getElementById("firebase_msg_select").disabled = true;
				document.getElementById("firebase_msg_select").style.background = 'grey';
			}
		}
    };
	xmlhttp.open("GET","script/script.php?task="+str,true);
  	xmlhttp.send();
}

function fillSelect(response) {
	
	if(!Array.isArray(response)){
		return false;
	}

	//var response = ["Saab", "Volvo", "BMW"];
	var select = document.getElementById("firebase_msg_select");

 	for(var i=0; i < response.length; i++) {
	   var opt = document.createElement("option");
	   opt.value= response[i];
	   opt.innerHTML = response[i]; // whatever property it has

	   // then append it to the select element
	   select.appendChild(opt);
	}
}

function form_submit(form){
	var phone = form.phone.value;
	
	if(phone == "")
		return false;
	
	sendNotify(phone);
}

function sendNotify(phone){

	var str = 'send_notify';

	if (window.XMLHttpRequest) {
    // code for modern browsers
    // IE7+, Firefox, Chrome, Opera, Safari
    	xmlhttp = new XMLHttpRequest();
 	} else {
    // code for old IE browsers
    // IE6, IE5
   		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}

	xmlhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
          
			var response = JSON.parse(this.responseText);
			
			if( !( (response == false) || (response == "false") ) ){
				document.getElementById("resultParagraph").innerHTML = "RESULT : " + this.responseText;
			}
			else {
				document.getElementById("resultParagraph").innerHTML = "ERROR RESULT : " + this.responseText;
			}
		}
    };
	xmlhttp.open("GET","script/script.php?task="+str + "&phone=" + phone,true);
  	xmlhttp.send();

}
