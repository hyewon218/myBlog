var main = {
	init : function () {
		var _this = this;
		$('#btn-update').on('click', function () {
			_this.update();
		});
	},

	update : function () {
		var data = {
			username: $('#username').val(),
			email: $('#email').val(),
			self_text: $('#self_text').val()
		};

		var id = $('#id').val();

		$.ajax({
			type: 'PUT',
			url: '/user/'+id+'/profile',
			dataType: 'json',
			contentType:'application/json; charset=utf-8',
			data: JSON.stringify(data)
		})/*.done(function() {
			alert('글이 수정되었습니다.');
			window.location.href = '/';
		}).fail(function (error) {
			alert(JSON.stringify(error));
		});*/
	},
};

main.init();