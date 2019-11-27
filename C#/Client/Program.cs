using Shared;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Threading;
using System.Threading.Tasks;

namespace Client
{
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        /// 
        [STAThread]
        static void Main()
        {
            Console.WriteLine("Client starting...");
            Loop();
        }

        private static int myID = -1;
        private static int hasBall = -1;
        private static SortedSet<int> players = new SortedSet<int>();

        static SocketClient client;

        static int readInt(String prompt)
        {
            string input = "";
            int num = 0;
            while (input.Trim().Length == 0 || num <= 0)
            {
                Console.Write(prompt);
                input = Console.ReadLine();
                int.TryParse(input, out num);
                return num;
            }

            return -1;
        }

        delegate void IntCallback(int val);
        delegate void Callback();

        static bool ParseThen(string obj, IntCallback ifTrue = null, Callback ifFalse = null)
        {
            int val;
            Console.WriteLine($"Parsing {obj}");
            if (int.TryParse(obj, out val))
            {
                ifTrue?.Invoke(val);
                return true;
            }
            else
            {
                ifFalse?.Invoke();
                return false;
            }
        }

        static void Loop()
        {
            try
            {
                SocketClient client = new SocketClient("localhost", 6969);
                Thread inputThread = null;

                Response res;
                while ((res = client.ReadLine()) != null)
                {
                    int ID = -1;
                    int.TryParse(res.data, out ID);

                    if (res.cmd != Constants.PING)
                        Console.Clear();

                    switch (res.cmd)
                    {
                        case Constants.PLAYER_JOIN:
                            ParseThen(res.data, p => { Console.WriteLine($"Player #{p} joined."); players.Add(p); });
                            break;

                        case Constants.PLAYER_LEAVE:
                            ParseThen(res.data, p => { Console.WriteLine($"Player #{p} left."); players.Remove(p); });
                            break;

                        case Constants.PLAYER_LIST:
                            players.Clear();
                            Console.WriteLine(res.data);
                            foreach (string player in res.data.Split(','))
                                ParseThen(player, p => players.Add(p));
                            break;

                        case Constants.BALL_MOVED:
                            if (ID >= 0)
                            {
                                hasBall = ID;
                                if (hasBall == myID) Console.WriteLine("Ball was passed to me!");
                                else Console.WriteLine($"Ball was passed to player #{hasBall}");
                            }
                            break;

                        case Constants.ID_ASSIGNED:
                            ParseThen(res.data,
                                (playerID) => { myID = playerID; Console.WriteLine($"I am Player #{playerID}"); },
                                        () => Console.Error.WriteLine("Invalid user ID from server"));
                            break;

                        case Constants.ERROR:
                            Console.Error.WriteLine(res.data);
                            break;

                        case Constants.PING:
                            continue;

                        default:
                            Console.Error.WriteLine($"Received invalid command from server: {res.message}");
                            break;
                    }

                    Console.WriteLine($"Players: {string.Join(", ", players)}");
                    if (hasBall != -1 && hasBall == myID)
                    {
                        Console.WriteLine("I have the ball! Who do I pass to?");
                        if (inputThread == null || !inputThread.IsAlive)
                        {
                            inputThread = new Thread(() =>
                            {
                                int passTo = -1;
                                while (passTo < 0 || !players.Contains(passTo))
                                {
                                    passTo = readInt("ID: ");
                                }
                                client.WriteLine(Constants.PASS_BALL, passTo);
                            });
                            inputThread.Start();
                        }
                    }
                    else Console.WriteLine($"Ball held by: {hasBall}");
                }
                Console.Error.WriteLine("Disconnected.");
            }
            catch (IOException e)
            {
                Console.Error.WriteLine(e.Message);
            }
        }
    }
}
