(function() { // avoid variables ending up in the global scope
	document.getElementById("login-form").addEventListener('submit', (e) => {

		e.preventDefault();

		let form = document.getElementById("login-form");
		let loginButton = document.getElementById("loginbutton");
		loginButton.disabled = true;

		if (form.checkValidity()) {
			makeCall("POST", 'Login', form,
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						let message = x.responseText;
						switch (x.status) {
							case 200:
								let response = JSON.parse(message);
								localStorage.setItem('username', response.username);
								localStorage.setItem('userId', response.id);
								window.location.href = "Home.html";
								break;
							default:
								alert(message);
								form.reset();
								loginButton.disabled = false;
								break;
						}
					}
				}
			);
		} else {
			form.reportValidity();
		}
	});
	
	document.getElementById("create").addEventListener('submit', (e) => {
		e.preventDefault();

		let form = document.getElementById("create");
		let createButton = document.getElementById("createaccountbutton");
		createButton.disabled = true;
		
		if (form.checkValidity()) {
			makeCall("POST", 'CreateAccount', form,
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						let message = x.responseText;
						switch (x.status) {
							case 200:
								alert("Successfully created account");
								form.reset();
								createButton.disabled = false;
								break;
							default:
								alert(message);
								form.reset();
								createButton.disabled = false;
								break;
						}
					}
				}
			);
		} else {
			form.reportValidity();
		}
	});
})();