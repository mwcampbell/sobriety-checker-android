import Image
import ImageDraw

for dpi, size in (("ldpi", 72), ("mdpi", 96), ("hdpi", 144)):
    for name, color in (("good", (0, 255, 0)),
                        ("slipping", (255, 255, 0)),
                        ("help_now", (255, 0, 0))):
        im = Image.new("RGB", (size, size))
        draw = ImageDraw.Draw(im)
        draw.ellipse((0, 0, size - 1, size - 1), fill=color)
        im.save("res/drawable-%s/ic_%s.png" % (dpi, name))
