window.onload = loadPhones();

function loadPhones() {
    receive();
}

/**
 * AL CARICAMENTO DELLA PAGINA RIEMPIAMO LA SELECT CON I NUMERI DI TELEFONO REGISTRATI NEL DATABASE
 */
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

			console.log(response);

			if( !( (response == false) || (response == "false") ) ){
				fillSelect(response);
			}
			else {
				document.getElementById("resultParagraph").innerHTML = " NO PHONE NUMBERS REGISTERED ";
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

	var select = document.getElementById("firebase_msg_select");

 	for(var i=0; i < response.length; i++) {
	   var opt = document.createElement("option");
	   opt.value= response[i].substring(0, response[i].indexOf('-') - 1);
	   opt.innerHTML = response[i];

	   select.appendChild(opt);
	}
}

/**
 * FUNZIONE INVOCATA AL SUBMIT DELLA FORM DI INVIO NOTIFICA
 */
function form_submit(form){
	var phone = form.phone.value;
	
	if(phone == ""){
		document.getElementById("resultParagraph").innerHTML = "PHONE NUMBER NOT SELECTED";
		return false;
	}
	
	sendNotify(phone);
}

/**
 * FUNZIONE CHE RICHIAMA script.php PER L'INVIO DELLA NOTIFICA FIREBASE
 */
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
			
			console.log(response);

			if( !( (response == false) || (response == "false") ) ){
				//alert(response);
				document.getElementById("resultParagraph").innerHTML = "FIREBASE SEND SUCCESSFUL";
			}
			else {
				//alert(response);
				document.getElementById("resultParagraph").innerHTML = "ERROR IN FIREBASE SEND";
			}
		}
    };
	xmlhttp.open("GET","script/script.php?task="+str + "&phone=" + phone,true);
  	xmlhttp.send();

}

/**
 * FUNZIONE INVOCATA AL SUBMIT DELLA FORM DI INVIO NOTIFICA A TUTTI
 */
function form_submit_msg_all(){

	var str = 'send_notify_all';

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
			
			console.log(response);
			
			if( !( (response == false) || (response == "false") ) ){
				document.getElementById("resultParagraph").innerHTML = "FIREBASE SEND ALL SUCCESSFUL";
			}
			else {
				//alert(response);
				document.getElementById("resultParagraph").innerHTML = "ERROR IN FIREBASE SEND ALL";
			}
		}
    };
	xmlhttp.open("GET","script/script.php?task="+str,true);
  	xmlhttp.send();
}
