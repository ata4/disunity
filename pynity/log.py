import logging

log = logging.getLogger()

try:
    # use optional colorlog dependency
    import colorlog

    formatter = colorlog.ColoredFormatter(
        "%(log_color)s%(levelname)-8s%(reset)s %(message)s",
        log_colors={
            'DEBUG':    'bold_cyan',
            'INFO':     'bold_green',
            'WARNING':  'bold_yellow',
            'ERROR':    'bold_red',
            'CRITICAL': 'bold_purple',
        }
    )

    handler = colorlog.StreamHandler()
    handler.setFormatter(formatter)

    log.addHandler(handler)
except ImportError:
    # use basic format
    logging.basicConfig(format="%(levelname)-8s %(message)s")

log.setLevel(logging.INFO)
