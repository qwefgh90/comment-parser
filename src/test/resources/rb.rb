module Rails
  # Returns the version of the currently loaded Rails as a <tt>Gem::Version</tt>
  def self.gem_version
    Gem::Version.new VERSION::STRING
  end
  
  module VERSION
    MAJOR = 5
    MINOR = 1
    TINY  = 0
    PRE   = "beta1"

    STRING = [MAJOR, MINOR, TINY, PRE].compact.join(".")
  end
end

=begin
can you see it?
mutiline comment
=end

a=%q(It can be non expanded.
Okay)
b=%Q(It can be expanded.
Okay)
