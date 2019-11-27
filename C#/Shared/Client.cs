using System;
using System.CodeDom;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace Shared
{
    public class SocketClient
    {
#if DEBUG
        static readonly bool DEBUG = true;
#else
        static readonly bool DEBUG = false;
#endif

        static int _Ids = 0;

        private StreamReader In;
        private StreamWriter Out;
        public int Id;


        public SocketClient(string address, int port)
        {
            TcpClient tcpClient = new TcpClient(address, port);
            NetworkStream stream = tcpClient.GetStream();
            In = new StreamReader(stream);
            Out = new StreamWriter(stream);
        }

        public SocketClient(Stream stream)
        {
            In = new StreamReader(stream);
            Out = new StreamWriter(stream);

            Id = ++_Ids;
        }

        public Response ReadLine()
        {
            try
            {
                String msg = In.ReadLine();
                if (DEBUG)
                    Console.WriteLine($"[FROM {Id}] {msg}");
                return new Response(msg);
            }
            catch (IOException e)
            {
                return null;
            }
        }

        public bool WriteLine(string command, object data)
        {
            String message = command;
            if (data != null)
                message += " " + data;

            Out.WriteLine(message);
            if (DEBUG)
                Console.WriteLine($"[TO {Id}] {message}");

            Out.Flush();

            return true;
        }

        public new String ToString()
        {
            if (Id == -1)
                return "SERVER";
            else
                return $"Player #{Id}";
        }
    }
}
