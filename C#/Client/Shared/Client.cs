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
    public class SClient
    {
        static readonly bool DEBUG = true;
        static int _Ids = 0;

        private TcpClient client;
        private StreamReader In;
        private StreamWriter Out;
        public int Id;


        public SClient(string address, int port)
        {
            TcpClient tcpClient = new TcpClient(address, port);
            NetworkStream stream = tcpClient.GetStream();
            In = new StreamReader(stream);
            Out = new StreamWriter(stream);
        }

        public SClient(Stream stream)
        {
            In = new StreamReader(stream);
            Out = new StreamWriter(stream);

            Id = ++_Ids;
        }

        public String ReadLine()
        {
            try
            {
                String msg = In.ReadLine();
                Console.WriteLine($"[FROM {Id}] {msg}");
                return msg;
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
