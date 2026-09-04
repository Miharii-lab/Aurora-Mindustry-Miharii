package aurora.core;

import java.io.IOException;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.io.SaveFileReader;

/** Persists Aurora ownership inside each Mindustry save file. */
public final class AuroraSaveData implements SaveFileReader.CustomChunk {
    public static final String CHUNK_NAME = "aurora.owner.v1";

    @Override
    public void write(java.io.DataOutput stream) throws IOException{
        int count = 0;
        for(Unit unit : Groups.unit){
            if(AuroraRegistry.isAurora(unit) && AuroraRegistry.profile(unit).hasOwner()) count++;
        }
        stream.writeInt(count);
        for(Unit unit : Groups.unit){
            if(!AuroraRegistry.isAurora(unit)) continue;
            AuroraProfile profile = AuroraRegistry.profile(unit);
            if(!profile.hasOwner()) continue;
            stream.writeInt(unit.id());
            stream.writeInt(profile.ownerId);
        }
    }

    @Override
    public void read(java.io.DataInput stream) throws IOException{
        int count = stream.readInt();
        if(count < 0 || count > 10000) throw new IOException("Invalid Aurora owner count: " + count);
        for(int i = 0; i < count; i++){
            int unitId = stream.readInt();
            int ownerId = stream.readInt();
            Unit unit = Groups.unit.getByID(unitId);
            if(unit != null && AuroraRegistry.isAurora(unit)){
                AuroraRegistry.profile(unit).ownerId = ownerId;
            }
        }
    }

    @Override
    public boolean writeNet(){
        return false;
    }
}
