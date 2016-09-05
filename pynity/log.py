import sys
import colorama

class Log:

    colorama.init(autoreset=True)

    trace_enabled = False

    @staticmethod
    def _print(*args, color, file=sys.stdout):
        msg = " ".join(str(a) for a in args)
        print(color + colorama.Style.BRIGHT + msg, file=file)

    @staticmethod
    def info(*args):
        Log._print(*args, color=colorama.Fore.WHITE)

    @staticmethod
    def trace(*args):
        if Log.trace_enabled:
            Log._print(*args, color=colorama.Fore.CYAN)

    @staticmethod
    def warning(*args):
        Log._print(*args, color=colorama.Fore.YELLOW, file=sys.stderr)

    @staticmethod
    def error(*args):
        Log._print(*args, color=colorama.Fore.RED, file=sys.stderr)