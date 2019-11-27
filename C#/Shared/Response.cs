using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Shared
{
    public class Response
    {

        public string cmd { get; private set; }
        public string data { get; private set; }
        public string message { get; private set; }

        public Response(string msg)
        {
            string[] split = msg.Split(new char[] { ' ' }, 2);

            message = msg;
            cmd = split[0];
            data = split.Length == 2 ? split[1] : null;
        }
    }
}
