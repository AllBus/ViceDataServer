package com.kos.voicedata

import java.awt.Robot
import java.awt.event.KeyEvent
import java.net.{Inet4Address, NetworkInterface}
import java.awt.AWTException
import java.awt.event.InputEvent
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import com.kos.voicedata.protobuf.voice._
import io.grpc.{ManagedChannelBuilder, Server, ServerBuilder}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

object Main {

	val PORT = 8452

	def main(args: Array[String]): Unit = {

		printIpAddresses()
		println(s"Port: $PORT")
		val server = new VoiceDataServer(ExecutionContext.global, PORT)
		server.start()
		server.blockUntilShutdown()
	}

	private def printIpAddresses(): Unit = {
		for {network ← NetworkInterface.getNetworkInterfaces.asScala
			 host ← network.getInetAddresses.asScala
		} {
			host match {
				case x:Inet4Address ⇒
					//if (x.isReachable(3000))
					System.out.println(x.getHostAddress)
				case _ ⇒
			}
		}
	}


	class VoiceDataServer(executor: ExecutionContext, val port: Int) {
		self ⇒

		private[this] var server: Server = null

		def start(): Unit = {
			server = ServerBuilder.forPort(port).
				addService(TransferGrpc.bindService(new TransferImpl, executor)).build.start

			sys.addShutdownHook {
				System.err.println("*** shutting down gRPC server since JVM is shutting down")
				self.stop()
				System.err.println("*** server shut down")
			}
			println("Start server")
		}

		def stop(): Unit = {
			if (server != null) {
				server.shutdown()
			}
		}

		def blockUntilShutdown(): Unit = {
			if (server != null) {
				server.awaitTermination()
			}
		}

		class TransferImpl extends TransferGrpc.Transfer {

			def press(req: SendData):Unit = {

				val text=req.data.voice.map(_.text).getOrElse("")
				if (text.isEmpty)
					return


				try {


					val stringSelection = new StringSelection(text+" ")
					val clpbrd = Toolkit.getDefaultToolkit.getSystemClipboard
					clpbrd.setContents(stringSelection, null)

					val robot = new Robot()
					// Simulate a mouse click
//					robot.mousePress(InputEvent.BUTTON1_MASK)
//					robot.mouseRelease(InputEvent.BUTTON1_MASK)


					// Simulate a key press


					robot.keyPress(KeyEvent.VK_CONTROL)
					robot.keyPress(KeyEvent.VK_V)
					robot.keyRelease(KeyEvent.VK_V)
					robot.keyRelease(KeyEvent.VK_CONTROL)
				} catch {
					case e: AWTException ⇒
						e.printStackTrace()
				}
			}

			override def say(req: SendData) = {
				println(req.data.voice.map(_.text).getOrElse(""))
				Future(press(req))(executor)

				val reply = TransferResponse(error=0,message="success")
				Future.successful(reply)
			}

		}

	}

}