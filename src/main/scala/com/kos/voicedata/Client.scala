package com.kos.voicedata

import java.util.concurrent.TimeUnit

import com.kos.voicedata.protobuf.voice.{SendData, TransferGrpc, TransferResponse}
import io.grpc.{ManagedChannel, ManagedChannelBuilder}

object Client {
	def main(args: Array[String]): Unit = {

		val a = protobuf.voice.VoiceText(text = "dfgdg ")

		val senddata = protobuf.voice.SendData(kind = 44).update(
			_.voice := a
		)

		val client = new TransferClient("127.0.0.1", Main.PORT)
		try{
			println(client.say(senddata).message)
		}finally{
			client.shutdown()
		}



	}

	class TransferClient(host: String, port: Int) {

		var channel: ManagedChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build()

		val blockingStub = TransferGrpc.blockingStub(channel)



		def shutdown(): Unit = {
			channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
		}

		def say(sendData: SendData): TransferResponse ={
			val r=blockingStub.say(sendData)
			println(s"to ${54 * 90} $sendData")
			r
		}
	}


}
