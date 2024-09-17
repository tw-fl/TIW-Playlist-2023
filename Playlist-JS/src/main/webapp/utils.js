function makeCall(method, url, formElement, cback, reset = true) {
	var req = new XMLHttpRequest(); // visible by closure
	req.onreadystatechange = function() {
		cback(req)
	}; // closure
	req.open(method, url);
	if (formElement == null) {
		req.send();
	} else {
		req.send(new FormData(formElement));
	}
	if (formElement !== null && reset === true) {
		formElement.reset();
	}
}


function formatDate(date) {
	return new Date(date).toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}