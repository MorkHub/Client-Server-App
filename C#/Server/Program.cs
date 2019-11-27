using Shared;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace Server
{
    class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        /// 
        [STAThread]
        static void Main()
        {
            Console.WriteLine("Server starting...");
            Loop();
        }

        private static readonly IDictionary<int, SocketClient> clients = new Dictionary<int, SocketClient>();

        private static void Broadcast(string command, object data)
        {
            foreach (var client in clients.Values)
            {
                client.WriteLine(command, data);
            }
        }

        private static int _hasBall = -1;

        static string ListPlayers()
        {
            return string.Join(",", clients.Values.Select(c => c.Id));
        }


        static bool FixBall()
        {
            lock (clients)
            {
                if (_hasBall == -1 || !clients.ContainsKey(_hasBall))
                {
                    if (clients.Count == 0)
                    {
                        _hasBall = -1;
                        Console.WriteLine(@"Ball returned to server");
                    }
                    else
                    {
                        foreach (var client in clients.Values)
                        {
                            _hasBall = client.Id;
                            break;
                        }

                        Console.WriteLine($@"Ball given to {_hasBall}");
                    }
                    Broadcast(Constants.BALL_MOVED, _hasBall);
                    return true;
                }
                return false;
            }
        }

        static void PassBall(SocketClient from, int id)
        {
            if (FixBall())
                Console.WriteLine(@"Fixed ball");

            if (_hasBall != -1 && _hasBall != from.Id)
            {
                from.WriteLine(Constants.ERROR, "You do not have the ball!");
                return;
            }

            if (clients.ContainsKey(id))
            {
                _hasBall = id;
                Broadcast(Constants.BALL_MOVED, _hasBall);
            }
            else
                from.WriteLine(Constants.ERROR, "Player not found.");
        }


        private static bool running = true;
        private static Thread _heartbeatThread;

        private static void Heartbeat()
        {
            if (_heartbeatThread == null)
            {
                _heartbeatThread = new Thread(() =>
                {
                    while (true)
                    {
                        foreach (var client in clients.Values)
                        {
                            client.WriteLine(Constants.PING, null);
                        }

                        Thread.Sleep(100);
                    }

                });

                _heartbeatThread.Start();
            }
        }


        static void Loop()
        {
            TcpListener server = new TcpListener(IPAddress.Loopback, 6969);
            server.Start();
            Console.WriteLine(@"Waiting for incoming connections...");
            while (running)
            {
                var tcpClient = server.AcceptTcpClient();

                var client = new SocketClient(tcpClient.GetStream());
                clients[client.Id] = client;

                client.WriteLine(Constants.ID_ASSIGNED, client.Id);
                client.WriteLine(Constants.PLAYER_LIST, ListPlayers());
                Broadcast(Constants.PLAYER_JOIN, client.Id);
                client.WriteLine(Constants.BALL_MOVED, _hasBall);


                Console.WriteLine($@" - {client.Id} connected: {ListPlayers()}");
                FixBall();

                new Thread(() =>
                {
                    using (Stream stream = tcpClient.GetStream())
                    {
                        try
                        {
                            Response res;
                            while ((res = client.ReadLine()) != null)
                            {
                                switch (res.cmd)
                                {
                                    case "PASS":
                                        int passTo;
                                        if (int.TryParse(res.data, out passTo))
                                            PassBall(client, passTo);
                                        else
                                            client.WriteLine(Constants.ERROR, "Invalid Player");
                                        break;

                                    default:
                                        throw new Exception($"Unknown command: {res.cmd}.");
                                }

                            }

                            clients.Remove(client.Id);
                            Broadcast(Constants.PLAYER_LEAVE, client.Id);

                            Console.WriteLine($" - {client.Id} disconnected {ListPlayers()}");
                            FixBall();
                        }
                        finally
                        {
                            stream.Close();
                        }
                    }
                }).Start();
            }
        }
    }
}
