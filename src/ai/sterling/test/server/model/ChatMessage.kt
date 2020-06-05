package ai.sterling.test.server.model

import java.text.SimpleDateFormat

data class ChatMessage(val id: Int, val username: String, val type: Int, var message: String, val date: Long){

	override fun toString(): String {
		var msg = when (type) {
			LOGIN -> {
				message = "$username just joined \n"
				message
			}
		
			LOGOUT -> {
				message = "$username has logged out.\n"
				message
			}
		
			MESSAGE -> {
				// this is different from the android client
				val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
				val time = simpleDateFormat.format(date)
				"$time $username: $message \n"
			}

			else -> "Invalid operation.\n"
		}
		
		return msg
	}
	
	companion object {
		var MESSAGE = 0
		var LOGOUT = 1
		var LOGIN = 2
	}
}