from PIL import Image
import sys

if __name__ == '__main__':
  filename = sys.argv[1]
  top = 0
  left = 2
  bottom = 0
  right = 0
  print("padding " + filename)
  im = Image.open(filename)
  im2 = Image.new(im.mode, (im.size[0] + left + right, im.size[1] + top + bottom), (0, 0, 0, 0))
  im2.paste(im, (left, top))
  im2.save(filename)
